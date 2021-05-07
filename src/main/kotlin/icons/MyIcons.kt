package icons

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.LayeredIcon

object MyIcons {

    @JvmField
    val RunRobot = IconLoader.getIcon("/icons/runRobot.svg", javaClass)

    @JvmField
    val ElementPass = IconLoader.getIcon("/icons/elementPass.svg", javaClass)

    @JvmField
    val ElementFail = IconLoader.getIcon("/icons/elementFail.svg", javaClass)

    @JvmField
    val ElementPass2 = IconLoader.getIcon("/icons/elementPass2.svg", javaClass)

    @JvmField
    val ElementFail2 = IconLoader.getIcon("/icons/elementFail2.svg", javaClass)

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
    val LabelInfo = IconLoader.getIcon("/icons/labelInfo.svg", javaClass)

    @JvmField
    val LabelDebug = IconLoader.getIcon("/icons/labelDebug.svg", javaClass)

    @JvmField
    val LabelTrace = IconLoader.getIcon("/icons/labelTrace.svg", javaClass)

    @JvmField
    val SuitePass = LayeredIcon(ElementPass, LabelSuite)

    @JvmField
    val SuiteFail = LayeredIcon(ElementFail, LabelSuite)

    @JvmField
    val TestPass = LayeredIcon(ElementPass, LabelTest)

    @JvmField
    val TestFail = LayeredIcon(ElementFail, LabelTest)

    @JvmField
    val SetupPass = LayeredIcon(ElementPass, LabelSetup)

    @JvmField
    val SetupFail = LayeredIcon(ElementFail, LabelSetup)

    @JvmField
    val TeardownPass = LayeredIcon(ElementPass2, LabelTeardown)

    @JvmField
    val TeardownFail = LayeredIcon(ElementFail2, LabelTeardown)

    @JvmField
    val KeywordPass = LayeredIcon(ElementPass2, LabelKeyword)

    @JvmField
    val KeywordFail = LayeredIcon(ElementFail2, LabelKeyword)

    @JvmField
    val ForPass = LayeredIcon(ElementPass, LabelFor)

    @JvmField
    val ForFail = LayeredIcon(ElementFail, LabelFor)

    @JvmField
    val ForitemPass = LayeredIcon(ElementPass, LabelForitem)

    @JvmField
    val ForitemFail = LayeredIcon(ElementFail, LabelForitem)

    @JvmField
    val StatusPass = LayeredIcon(ElementPass, LabelPass).scale(1.5F)

    @JvmField
    val StatusFail = LayeredIcon(ElementFail, LabelFail).scale(1.5F)

    @JvmField
    val LevelInfo = LayeredIcon(ElementPass, LabelInfo)

    @JvmField
    val LevelDebug = LayeredIcon(ElementPass, LabelDebug)

    @JvmField
    val LevelTrace = LayeredIcon(ElementPass, LabelTrace)

    @JvmField
    val LevelFail = LayeredIcon(ElementPass, LabelFail)
}