clojure -M -e "(compile 'core)"
bb -cp $(clojure -Spath) uberjar ktor-generator.jar -m core