include(macro.m4)
### Breaking changes
This release introduces a breaking change - due to deprecation of DelayedInit
afterInit hack had to be removed, thus ScallopConf no longer automatically
calls verify() at the end of class initialization - you have to call it explicity.
LazyScallopConf is now removed as well, since now you can call verify() where you want.

Also, subcommands have to be explicitly added to parent config via addSubcommand() call.

While usage is a little less streamlined now, this change led to removal
of some tricky and buggy magic from the library, making it more stable -
now you can factor your options any way you want, use option mixins, move subcommand
definitions into separate source files, etc.

### Improvements
* Cross-build for Scala 2.12 milestone (I(99)).
* Better error messages for trailing args parse failure (I(87)).
* Improved option name guessing - now it correctly handles derived options and
  fails hard if guessing failed due to reflection quirks.
* Added "--opt=value" argument style (I(94)).
* Added unnamed integer options, like seen in GNU tail (I(71)).
* Fixed inefficiency in option list parsing.
* Removed auto-generated default value display in help output.
