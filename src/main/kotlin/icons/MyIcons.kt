package icons

import com.intellij.openapi.util.IconLoader

object MyIcons {

    @JvmField
    val RunRobot = IconLoader.getIcon("/icons/runRobot.svg", javaClass)

    @JvmField
    val TestPass = IconLoader.getIcon("/icons/testPass.svg", javaClass)

    @JvmField
    val TestFail = IconLoader.getIcon("/icons/testFail.svg", javaClass)

}