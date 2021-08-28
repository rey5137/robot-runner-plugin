import socket
import json
from typing import Any
from enum import Enum


class RobotRunnerPluginListener:

    ROBOT_LISTENER_API_VERSION = 2

    def __init__(self, port):
        self.client = Client('127.0.0.1', int(port))

    def start_suite(self, name, attrs):
        self.client.request("start_suite", {'name': name, 'attrs': attrs})

    def start_test(self, name, attrs):
        self.client.request("start_test", {'name': name, 'attrs': attrs})

    def start_keyword(self, name, attrs):
        self.client.request("start_keyword", {'name': name, 'attrs': attrs})

    def log_message(self, message):
        self.client.request("log_message", message)

    def end_keyword(self, name, attrs):
        self.client.request("end_keyword", {'name': name, 'attrs': attrs})

    def end_test(self, name, attrs):
        self.client.request("end_test", {'name': name, 'attrs': attrs})

    def end_suite(self, name, attrs):
        self.client.request("end_suite", {'name': name, 'attrs': attrs})

    def close(self):
        self.client.request("close", {})


class Client:

    def __init__(self, host: str, port: int):
        self.family = socket.AF_INET
        self.host = host
        self.port = port
        self.path = None

    @classmethod
    def from_unix(cls, path: str):
        ins = cls('', 0)
        ins.family = socket.AF_UNIX
        ins.path = path
        return ins

    @classmethod
    def from_inet(cls, host: str, port: int):
        return cls(host, port)

    def request(self, method: str, payload: Any = None) -> Any:
        if self.path is not None:
            s = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
            s.connect(self.path)
        else:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect((self.host, self.port))
        try:
            write_request(s, method, payload)
            code, result = read_response(s)
            if code == StatusCode.GOOD_RESPONSE:
                return result
            else:
                raise ServerError(result)
        finally:
            s.close()


class Versions(Enum):
    V1_0 = b'\x01\x00'
    V1_1 = b'\x01\x01'

    @staticmethod
    def current():
        return Versions.V1_1


class StatusCode(Enum):
    REQUEST = 0
    GOOD_RESPONSE = 1
    BAD_RESPONSE = 2


class Keys(Enum):
    METHOD = 'method'
    PAYLOAD = 'payload'
    MESSAGE = 'message'


class ErrorMessages:
    UNRECOGNIZED_PROTOCOL = 'unrecognized protocol'
    INCOMPATIBLE_VERSION = 'incompatible protocol version'
    INCOMPLETE_DATA = 'incomplete data'
    INVALID_STATUS_CODE = 'invalid status code'
    INVALID_BODY = 'invalid body'
    UNKNOWN_SERVER_ERROR = 'unknown server error'


class ProtocolError(Exception):
    def __init__(self, message: str, detail: str = None):
        self.message = message
        self.detail = detail


class ServerError(Exception):
    def __init__(self, message: str):
        self.message = message


def read_bytes(s: socket.socket, count: int) -> bytes:
    rst = b''
    if count == 0:
        return rst
    while True:
        tmp = s.recv(count - len(rst))
        if len(tmp) == 0:
            break
        rst += tmp
        if len(rst) == count:
            break
    return rst


def read_socket(s: socket.socket) -> (int, dict):
    # 1. FLAG 'pb'
    flag = read_bytes(s, 2)
    if flag != b'pb':
        raise ProtocolError(ErrorMessages.UNRECOGNIZED_PROTOCOL)

    # 2. VERSION
    ver = read_bytes(s, 2)
    if ver != Versions.current().value:
        raise ProtocolError(ErrorMessages.INCOMPATIBLE_VERSION,
                            "need version {} but found {}".format(Versions.current(), ver))

    # 3. STATUS CODE
    status_code = read_bytes(s, 1)
    if len(status_code) != 1:
        raise ProtocolError(ErrorMessages.INCOMPLETE_DATA)
    code = status_code[0]

    # 4. RESERVED (2 bytes)
    reserved = read_bytes(s, 2)
    if len(reserved) != 2:
        raise ProtocolError(ErrorMessages.INCOMPLETE_DATA)

    # 5. LENGTH (4-byte, little endian)
    len_bytes = read_bytes(s, 4)
    if len(len_bytes) != 4:
        raise ProtocolError(ErrorMessages.INCOMPLETE_DATA)
    json_len = len_bytes[0]
    json_len += len_bytes[1] << 8
    json_len += len_bytes[2] << 16
    json_len += len_bytes[3] << 24

    # 6. JSON OBJECT
    text_bytes = read_bytes(s, json_len)
    if len(text_bytes) != json_len:
        raise ProtocolError(ErrorMessages.INCOMPLETE_DATA,
                            'expect ' + str(json_len) + ' bytes but found ' + str(len(text_bytes)))
    try:
        obj = json.loads(str(text_bytes, encoding='utf-8'), encoding='utf-8')
    except Exception as err:
        raise ProtocolError(ErrorMessages.INVALID_BODY, "{}".format(err))

    return code, obj


def write_socket(s: socket.socket, status_code: StatusCode, json_obj: dict):
    # 1. FLAG
    s.sendall(b'pb')
    # 2. VERSION
    s.sendall(Versions.current().value)
    # 3. STATUS CODE
    s.sendall(bytes([status_code.value]))
    # 4. RESERVED 2 BYTES
    s.sendall(b'\x00\x00')

    # 5. LENGTH (little endian)
    json_text = json.dumps(json_obj)
    json_bytes = bytes(json_text, encoding='utf-8')
    len_bytes = len(json_bytes).to_bytes(4, byteorder='little')
    s.sendall(len_bytes)

    # 6. JSON
    s.sendall(json_bytes)


def write_request(s: socket.socket, method: str, payload: Any):
    body = {}
    if method is not None:
        body[Keys.METHOD.value] = method
    if payload is not None:
        body[Keys.PAYLOAD.value] = payload
    write_socket(s, StatusCode.REQUEST, body)


def read_response(s: socket.socket) -> (StatusCode, Any):
    status_code, obj = read_socket(s)
    if status_code == StatusCode.GOOD_RESPONSE.value:
        if Keys.PAYLOAD.value not in obj:
            return StatusCode.GOOD_RESPONSE, None
        else:
            return StatusCode.GOOD_RESPONSE, obj[Keys.PAYLOAD.value]
    elif status_code == StatusCode.BAD_RESPONSE.value:
        if Keys.MESSAGE.value not in obj:
            return StatusCode.BAD_RESPONSE, ErrorMessages.UNKNOWN_SERVER_ERROR
        else:
            return StatusCode.BAD_RESPONSE, str(obj[Keys.MESSAGE.value])
    else:
        raise ProtocolError(ErrorMessages.INVALID_STATUS_CODE, "{}".format(status_code))