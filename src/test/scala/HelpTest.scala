package org.rogach.scallop

class HelpTest extends UsefulMatchers with CapturingTest {
  throwError.value = false

  test ("help printing") {
    val (out, err) = captureOutput {
      val exits = trapExit {
        new ScallopConf(List("--help")) {
          version("0.1.2")
          banner("some rubbish")
          footer("and some more")
          val apples = opt[Int]("apples", descr = "fresh apples!", default = Some(3))
          val verbose = toggle("verbose", descrYes = "very verbose", descrNo = "turn off")

          val tree = new Subcommand("tree") {
            val branches = opt[Int]("branches", descr = "how many branches?")
            val trail = trailArg[String]("trail", descr = "Which trail do you choose?")
          }
          addSubcommand(tree)

          val peach = new Subcommand("peach") {
            banner("plant the fruit-bearing peach tree")
            val peaches = opt[Int]("peaches", descr = "how many peaches?")
            footer("Latin name: Prunus persica\n")
          }
          addSubcommand(peach)

          val palm = new Subcommand("palm") {
            val leaves = opt[Int]("leaves", descr = "how many leaves?")
          }
          addSubcommand(palm)

          verify()
        }
      }
      exits.size should equal (1)
    }
    out should equal ("""0.1.2
                        |some rubbish
                        |  -a, --apples  <arg>   fresh apples!
                        |  -v, --verbose         very verbose
                        |      --help            Show help message
                        |      --version         Show version of this program
                        |
                        |Command: tree
                        |  -b, --branches  <arg>   how many branches?
                        |      --help              Show help message
                        |
                        | trailing arguments:
                        |  trail (required)   Which trail do you choose?
                        |Command: peach
                        |plant the fruit-bearing peach tree
                        |  -p, --peaches  <arg>   how many peaches?
                        |      --help             Show help message
                        |Latin name: Prunus persica
                        |
                        |Command: palm
                        |  -l, --leaves  <arg>   how many leaves?
                        |      --help            Show help message
                        |and some more
                        |""".stripMargin)
  }

  test ("subcommand description in output") {
    val (out, err) = captureOutput {
      val exits = trapExit {
        new ScallopConf(List("--help")) {
          version("0.1.2")
          val apples = opt[Int]("apples", descr = "fresh apples!")

          val tree = new Subcommand("tree") {
            descr("some tree")
            val branches = opt[Int]("branches", descr = "how many branches?")
          }
          addSubcommand(tree)

          verify()
        }
      }
      exits.size should equal (1)
    }
    out should equal ("""0.1.2
                        |  -a, --apples  <arg>   fresh apples!
                        |      --help            Show help message
                        |      --version         Show version of this program
                        |
                        |Command: tree - some tree
                        |  -b, --branches  <arg>   how many branches?
                        |      --help              Show help message
                        |""".stripMargin)
  }

  test ("help wrapping") {
    val opts = Scallop()
      .version("")
      .opt[Boolean]("apples", descr = "********* ********* ********* ********* ********* *********")
    opts.setHelpWidth(80).help should equal ("""  -a, --apples    ********* ********* ********* ********* ********* *********
                                               |      --help      Show help message
                                               |      --version   Show version of this program""".stripMargin)
    opts.setHelpWidth(40).help should equal ("""  -a, --apples    ********* *********
                                               |                  ********* *********
                                               |                  ********* *********
                                               |      --help      Show help message
                                               |      --version   Show version of this
                                               |                  program""".stripMargin)
  }

  test ("version printing") {
    val (out, err) = captureOutput {
      val exits = trapExit {
        new ScallopConf(List("--version")) {
          version("0.1.2")
          val apples = opt[Int]("apples")
          verify()
        }
      }
      exits.size should equal (1)
    }
    out should equal ("""0.1.2
                        |""".stripMargin)
  }

  test ("help for subcommand") {
    val (out, err, exits) = captureOutputAndExits {
      new ScallopConf(Seq("tree", "--help")) {
        val tree = new Subcommand("tree") {
          banner("Planting a tree")
          val apples = opt[Int](descr = "how many apples?")
          val jang = trailArg[String]("jang")
          footer("finish planting.")
        }
        addSubcommand(tree)

        verify()
      }
    }

    exits ==== List(0)
    err ==== ""
    out ====
     """Planting a tree
       |  -a, --apples  <arg>   how many apples?
       |      --help            Show help message
       |
       | trailing arguments:
       |  jang (required)
       |finish planting.
       |""".stripMargin
  }

  test ("help for subcommand two levels deep") {
    val (out, err, exits) = captureOutputAndExits {
      new ScallopConf(Seq("tree", "peach", "--help")) {
        val tree = new Subcommand("tree") {
          val peach = new Subcommand("peach") {
            val apples = opt[Int](descr = "how many apples?")
          }
          addSubcommand(peach)
        }
        addSubcommand(tree)

        verify()
      }
    }

    exits ==== List(0)
    err ==== ""
    out ====
     """  -a, --apples  <arg>   how many apples?
       |      --help            Show help message
       |""".stripMargin

  }

  test ("short format for subcommands help") {
    val (out, _, _) = captureOutputAndExits {
      new ScallopConf(Seq("--help")) {
        banner("Planting a tree.")
        shortSubcommandsHelp()
        val bananas = opt[Int]("bananas")

        val tree = new Subcommand("tree") {
          descr("Plant a normal, regular tree")
          val apples = opt[Int]("apples")
        }
        addSubcommand(tree)

        val peach = new Subcommand("peach") {
          descr("Plant a peach tree.")
        }
        addSubcommand(peach)

        val submarine = new Subcommand("submarine") {()}
        addSubcommand(submarine)

        verify()
      }
    }

    out ====
      """Planting a tree.
        |  -b, --bananas  <arg>
        |      --help             Show help message
        |
        |Commands:
        |  tree        Plant a normal, regular tree
        |  peach       Plant a peach tree.
        |  submarine
        |""".stripMargin
  }

  test ("splitting commands list into 'main' and 'other' options") {
    object Conf extends ScallopConf(Nil) {
      mainOptions = Seq(bananas, apples)
      val apples     = opt[Int](descr = "amount of apples")
      val bananas    = opt[Int](descr = "amount of bananas")
      val coconuts   = opt[Int](descr = "amount of coconuts")
      val dewberries = opt[Int](descr = "amount of dewberries")

      verify()
    }
    Conf.builder.help ====
      """  -b, --bananas  <arg>      amount of bananas
        |  -a, --apples  <arg>       amount of apples
        |
        |  -c, --coconuts  <arg>     amount of coconuts
        |  -d, --dewberries  <arg>   amount of dewberries
        |      --help                Show help message""".stripMargin
  }

  test ("splitting commands list into 'main' and 'other' options (in subcommands)") {
    object Conf extends ScallopConf(Nil) {
      val plant = new Subcommand("plant") {
        mainOptions = Seq(bananas, apples)
        val apples     = opt[Int](descr = "amount of apples")
        val bananas    = opt[Int](descr = "amount of bananas")
        val coconuts   = opt[Int](descr = "amount of coconuts")
        val dewberries = opt[Int](descr = "amount of dewberries")

      }
      addSubcommand(plant)

      verify()
    }
    Conf.plant.builder.help ====
      """  -b, --bananas  <arg>      amount of bananas
        |  -a, --apples  <arg>       amount of apples
        |
        |  -c, --coconuts  <arg>     amount of coconuts
        |  -d, --dewberries  <arg>   amount of dewberries
        |      --help                Show help message""".stripMargin
  }

  test ("user-provided help & version option takes precedence over hardcoded one") {
    object Conf extends ScallopConf(Nil) {
      val help = opt[Boolean](noshort = true, descr = "custom help descr")
      val version = opt[Boolean](noshort = true, descr = "custom version descr")

      verify()
    }
    Conf.builder.help ====
      """      --help      custom help descr
        |      --version   custom version descr""".stripMargin
  }

  test ("user-provided help option works with short-named argument") {
    val (out, err) = captureOutput {
      val exits = trapExit {
        new ScallopConf(Seq("-?")) {
          val help = opt[Boolean]("help", short = '?', descr = "custom help descr")

          verify()
        }
      }
      exits.size ==== 1
    }
    out ====
      """  -?, --help   custom help descr
        |""".stripMargin
  }

  test ("nested subcommands are reflected in help") {
    val (out, err) = captureOutput {
      val exits = trapExit {
        object Conf extends ScallopConf(List("--help")) {
          val tree = new Subcommand("tree") {
            val branches = opt[Int]("branches", descr = "how many branches?")
            val trail = trailArg[String]("trail", descr = "Which trail do you choose?")

            val peach = new Subcommand("peach") {
              banner("plant the fruit-bearing peach tree")
              val peaches = opt[Int]("peaches", descr = "how many peaches?")

              val palm = new Subcommand("palm") {
                val leaves = opt[Int]("leaves", descr = "how many leaves?")
              }
              addSubcommand(palm)

              footer("Latin name: Prunus persica\n")
            }
            addSubcommand(peach)
          }
          addSubcommand(tree)

          verify()
        }
        Conf
      }
      exits.size should equal (1)
    }
    out should equal ("""      --help   Show help message
                        |
                        |Command: tree
                        |  -b, --branches  <arg>   how many branches?
                        |      --help              Show help message
                        |
                        | trailing arguments:
                        |  trail (required)   Which trail do you choose?
                        |
                        |Command: tree peach
                        |plant the fruit-bearing peach tree
                        |  -p, --peaches  <arg>   how many peaches?
                        |      --help             Show help message
                        |
                        |Command: tree peach palm
                        |  -l, --leaves  <arg>   how many leaves?
                        |      --help            Show help message
                        |Latin name: Prunus persica
                        |""".stripMargin)
  }

  test ("append default values to help output if requested") {
    object Conf extends ScallopConf(Nil) {
      appendDefaultToDescription = true
      val apples = opt[Int](descr = "amount of apples", default = Some(42))
      verify()
    }
    Conf.builder.help ====
      """  -a, --apples  <arg>   amount of apples (default = 42)
        |      --help            Show help message""".stripMargin
  }

  test ("append default values to help output of subcommand if parent has this setting") {
    val (out, _, _) = captureOutputAndExits {
      new ScallopConf(Seq("--help")) {
        appendDefaultToDescription = true
        val tree = new Subcommand("tree") {
          val apples = opt[Int](descr = "how many apples?", default = Some(42))
        }
        addSubcommand(tree)

        verify()
      }
    }

    out ====
     """      --help   Show help message
       |
       |Command: tree
       |  -a, --apples  <arg>   how many apples? (default = 42)
       |      --help            Show help message
       |""".stripMargin

  }

  test ("handle default value printing in subcommand help") {
    val (out, _, _) = captureOutputAndExits {
      new ScallopConf(Seq("tree", "--help")) {
        appendDefaultToDescription = true
        val tree = new Subcommand("tree") {
          val apples = opt[Int](descr = "how many apples?", default = Some(42))
        }
        addSubcommand(tree)

        verify()
      }
    }

    out ====
     """  -a, --apples  <arg>   how many apples? (default = 42)
       |      --help            Show help message
       |""".stripMargin

  }
}
