package com.github.rey5137.robotrunnerplugin.editors

import com.intellij.util.xml.DomFileDescription

class RobotOutputDomFileDescription: DomFileDescription<RobotDomElement>(RobotDomElement::class.java, "robot")