package icons

import com.intellij.icons.AllIcons
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.LayeredIcon

object MyIcons {

    @JvmField
    val RunRobot = IconLoader.getIcon("/icons/runRobot.svg", javaClass)

    @JvmField
    val ArrowDown = IconLoader.getIcon("/icons/arrowDown.svg", javaClass)

    @JvmField
    val ArrowDownWhite = IconLoader.getIcon("/icons/arrowDownWhite.svg", javaClass)

    @JvmField
    val ArrowRight = IconLoader.getIcon("/icons/arrowRight.svg", javaClass)

    @JvmField
    val ArrowRightWhite = IconLoader.getIcon("/icons/arrowRightWhite.svg", javaClass)

    @JvmField
    val ElementPass = IconLoader.getIcon("/icons/elementPass.svg", javaClass)

    @JvmField
    val ElementFail = IconLoader.getIcon("/icons/elementFail.svg", javaClass)

    @JvmField
    val ElementRunning = IconLoader.getIcon("/icons/elementRunning.svg", javaClass)

    @JvmField
    val ElementPass2 = IconLoader.getIcon("/icons/elementPass2.svg", javaClass)

    @JvmField
    val ElementFail2 = IconLoader.getIcon("/icons/elementFail2.svg", javaClass)

    @JvmField
    val ElementRunning2 = IconLoader.getIcon("/icons/elementRunning2.svg", javaClass)

    @JvmField
    val LabelSuite = IconLoader.getIcon("/icons/labelSuite.svg", javaClass)

    @JvmField
    val LabelTest = IconLoader.getIcon("/icons/labelTest.svg", javaClass)

    @JvmField
    val LabelKeyword = IconLoader.getIcon("/icons/labelKeyword.svg", javaClass)

    @JvmField
    val LabelSetup = IconLoader.getIcon("/icons/labelSetup.svg", javaClass)

    @JvmField
    val LabelTeardown = IconLoader.getIcon("/icons/labelTeardown.svg", javaClass)

    @JvmField
    val LabelFor = IconLoader.getIcon("/icons/labelFor.svg", javaClass)

    @JvmField
    val LabelForitem = IconLoader.getIcon("/icons/labelForitem.svg", javaClass)

    @JvmField
    val LabelPass = IconLoader.getIcon("/icons/labelPass.svg", javaClass)

    @JvmField
    val LabelFail = IconLoader.getIcon("/icons/labelFail.svg", javaClass)

    @JvmField
    val LabelRunning = IconLoader.getIcon("/icons/labelRunning.svg", javaClass)

    @JvmField
    val LabelInfo = IconLoader.getIcon("/icons/labelInfo.svg", javaClass)

    @JvmField
    val LabelDebug = IconLoader.getIcon("/icons/labelDebug.svg", javaClass)

    @JvmField
    val LabelTrace = IconLoader.getIcon("/icons/labelTrace.svg", javaClass)

    @JvmField
    val LabelError = IconLoader.getIcon("/icons/labelError.svg", javaClass)

    @JvmField
    val SuitePass = LayeredIcon(ElementPass, LabelSuite)

    @JvmField
    val SuiteFail = LayeredIcon(ElementFail, LabelSuite)

    @JvmField
    val SuiteRunning = LayeredIcon(ElementRunning, LabelSuite)

    @JvmField
    val TestPass = LayeredIcon(ElementPass, LabelTest)

    @JvmField
    val TestFail = LayeredIcon(ElementFail, LabelTest)

    @JvmField
    val TestRunning = LayeredIcon(ElementRunning, LabelTest)

    @JvmField
    val SetupPass = LayeredIcon(ElementPass, LabelSetup)

    @JvmField
    val SetupFail = LayeredIcon(ElementFail, LabelSetup)

    @JvmField
    val SetupRunning = LayeredIcon(ElementRunning, LabelSetup)

    @JvmField
    val TeardownPass = LayeredIcon(ElementPass2, LabelTeardown)

    @JvmField
    val TeardownFail = LayeredIcon(ElementFail2, LabelTeardown)

    @JvmField
    val TeardownRunning = LayeredIcon(ElementRunning2, LabelTeardown)

    @JvmField
    val KeywordPass = LayeredIcon(ElementPass2, LabelKeyword)

    @JvmField
    val KeywordFail = LayeredIcon(ElementFail2, LabelKeyword)

    @JvmField
    val KeywordRunning = LayeredIcon(ElementRunning2, LabelKeyword)

    @JvmField
    val ForPass = LayeredIcon(ElementPass, LabelFor)

    @JvmField
    val ForFail = LayeredIcon(ElementFail, LabelFor)

    @JvmField
    val ForRunning = LayeredIcon(ElementRunning, LabelFor)

    @JvmField
    val ForitemPass = LayeredIcon(ElementPass, LabelForitem)

    @JvmField
    val ForitemFail = LayeredIcon(ElementFail, LabelForitem)

    @JvmField
    val ForitemRunning = LayeredIcon(ElementRunning, LabelForitem)

    @JvmField
    val StatusPass = LayeredIcon(ElementPass, LabelPass).scale(1.5F)

    @JvmField
    val StatusFail = LayeredIcon(ElementFail, LabelFail).scale(1.5F)

    @JvmField
    val StatusRunning = LayeredIcon(ElementRunning, LabelRunning).scale(1.5F)

    @JvmField
    val LevelInfo = LayeredIcon(ElementPass, LabelInfo)

    @JvmField
    val LevelDebug = LayeredIcon(ElementPass, LabelDebug)

    @JvmField
    val LevelTrace = LayeredIcon(ElementPass, LabelTrace)

    @JvmField
    val LevelFail = LayeredIcon(ElementFail, LabelFail)

    @JvmField
    val LevelError = LayeredIcon(ElementFail, LabelError)

    @JvmField
    val OpenFile = IconLoader.getIcon("/icons/openFile.svg", javaClass)

    @JvmField
    val OpenFileWhite = IconLoader.getIcon("/icons/openFileWhite.svg", javaClass)
}