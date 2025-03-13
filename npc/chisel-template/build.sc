// import Mill dependency
import mill._
import mill.define.Sources
import mill.modules.Util
import mill.scalalib.TestModule.ScalaTest
import scalalib._
// support BSP
import mill.bsp._

object coretop extends SbtModule { m =>
  override def millSourcePath = os.pwd
  override def scalaVersion = "2.13.15"
  override def scalacOptions = Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-feature",
    "-Xcheckinit",
  )
  override def ivyDeps = Agg(
    ivy"org.chipsalliance::chisel:6.6.0",
  )
  override def scalacPluginIvyDeps = Agg(
    ivy"org.chipsalliance:::chisel-plugin:6.6.0",
  )
    // 测试模块
  object test extends ScalaTests { 
    def ivyDeps = super.ivyDeps() ++ Agg(  // 合并主模块依赖
      ivy"org.scalatest::scalatest:3.2.16",
      ivy"edu.berkeley.cs::chiseltest:6.0.0"  // 注意包名是 edu.berkeley.cs
    )
    def testFramework = "org.scalatest.tools.Framework"
  }

}
