Jeff's data class generator


TODO:  We want to somehow make it possible for users to generate the following script
within /usr/local/bin/datagen :

```
#!/usr/bin/env sh
set -eu

java -jar $HOME/.m2/repository/com/jsbase/datagen/1.0/datagen-1.0-jar-with-dependencies.jar "$@"
```
