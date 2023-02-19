from robot.api.deco import library, keyword
from robot.libraries.BuiltIn import BuiltIn


@library(scope='GLOBAL', version='0.1.0', auto_keywords=False)
class RobotStepLibrary:
    """Library for marking step in a test case
    This library can be used as standalone, but will give better visual if used with Robot Runner Plugin
    """

    @keyword('Step')
    def step(self, num: str, title: str = '', *others):
        """Mark the start of a step.

        You can also pass a single keyword that will be executed when this keyword run.

        Examples:
        | Step | 1 |
        | Step | 2 | This is a step with title
        | Step | 3 | This is a step with keyword | should be equal  1   1
        """
        if others:
            name = others[0]
            others = others[1:]
            BuiltIn().run_keyword(name, *others)
        pass

    @keyword('End step')
    def end_step(self, num: str, title: str = '', *others):
        """Mark the end of a step.

        You can also pass a single keyword that will be executed when this keyword run.

        Examples:
        | End step | 1 |
        | End step | 2 | End step with title
        | End step | 3 | End step with keyword | should be equal  1   1
        """
        if others:
            name = others[0]
            others = others[1:]
            BuiltIn().run_keyword(name, *others)
        pass
