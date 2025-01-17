(defproject tupelo "21.11.06a"
  :description "Tupelo:  Clojure With A Spoonful of Honey"
  :url "https://github.com/cloojure/tupelo"
  :scm "https://github.com/cloojure/tupelo"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.9.5"

  :global-vars {*warn-on-reflection* false}

  :excludes [org.clojure/clojure
             org.clojure/clojurescript]

  :dependencies
  [
   ; temp dev libs
   ; [org.clojure/clojure "1.10.3"]

   ; top-priority libs
   [org.clojure/core.async "1.3.622"]
   [org.clojure/core.match "1.0.0"]
   [org.clojure/data.avl "0.1.0"]
   [org.clojure/data.csv "1.0.0"]
   [org.clojure/data.xml "0.2.0-alpha6"]
   [org.clojure/math.combinatorics "0.1.6"]
   [org.clojure/spec.alpha "0.2.194"]
   [org.clojure/test.check "1.1.0"]
   [org.clojure/tools.reader "1.3.6"]

   ; other libs
   [camel-snake-kebab "0.4.2"]
   [cheshire "5.10.1"] ; #todo switch to muuntaja/jsonista
   [clj-time "0.15.2"]
   [danlentz/clj-uuid "0.1.9"]
   [joda-time/joda-time "2.10.12"]
   [org.ccil.cowan.tagsoup/tagsoup "1.2.1"]
   [org.flatland/ordered "1.5.9"]
   [org.snakeyaml/snakeyaml-engine "2.3"]
   [prismatic/schema "1.1.12"]

   ; #todo clojures pprint doesn't work under graal native-image
   ]

  ; Using `lein-ancient check :all` checks plugins
  :plugins [
            ;[lein-cljsbuild "1.1.8"]
            [lein-codox "0.10.7"]
            ;[lein-nomis-ns-graph "0.14.2"]
            [com.jakemccrary/lein-test-refresh "0.24.1"]]

  :test-refresh {:quiet true ; true => suppress printing namespaces when testing
                 }

  :codox {:src-dir-uri               "http://github.com/cloojure/tupelo/blob/master/"
          :src-linenum-anchor-prefix "L"
          :source-paths              ["src/clj" "src/cljc"]
          :language                  :clojure
          :namespaces                [#"^tupelo\."]
          :exclude-vars              #".*IntervalClosed|.*SpliceItem|map->[A-Z].*"
          }

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                                       [org.clojure/clojurescript "1.10.597" :scope "provided"]
                                       ]}
             :dev      {:dependencies [
                                       ;[binaryage/devtools "0.9.10"]
                                       ;[binaryage/dirac "1.4.3"]
                                       ;[com.cemerick/piggieback "0.2.2"]
                                       [criterium "0.4.6"]
                                      ;[org.clojure/clojure "1.10.3"]
                                       [org.clojure/clojure "1.11.0-alpha1"]
                                      ;[org.clojure/clojurescript  "1.10.764"] ; "1.10.597"
                                       ]
                        }
             :test     {:dependencies [[overtone/at-at "1.2.0"]]}
             :1.8      {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9      {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.10      {:dependencies [[org.clojure/clojure "1.10.0"]]}
             }
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]
  :target-path "target/%s"

  ; need to add the compliled assets to the :clean-targets
  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "out"
                                    :target-path]

  ; "lein test"         will not  run tests marked with the ":slow" metadata
  ; "lein test :slow"   will only run tests marked with the ":slow" metadata
  ; "lein test :all"    will run all  tests (built-in)
  :test-selectors {:default (complement :slow)
                   :slow    :slow
                   :fast    :fast}
  ; #todo broken for tupelo.test/dospec - why?

  ; :main ^:skip-aot tupelo.core
  ; :uberjar      {:aot :all}

  ; :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  ; automatically handle `--add-modules` stuff req'd for Java 9 & Java 10
  :jvm-opts ["-Xms500m" "-Xmx2g"
             ; "--illegal-access=permit"  ; may need for Java10+  [ permit, warn, debug, deny ]
             ]
  )
















