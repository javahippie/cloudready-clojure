(def project 'scrapbook)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.10.0"]
                            [adzerk/boot-test "1.2.0" :scope "test"]
                            [ring "1.8.0"]
                            [rum "0.11.4"]
                            [yogthos/config "1.1.7"]
                            [ring/ring-defaults "0.3.2"]
                            [org.postgresql/postgresql "42.2.11"]
                            [jdbc-ring-session "1.3"]
                            [ring/ring-session-timeout "0.2.0"]
                            [migratus "1.2.8"]])

(task-options!
 aot {:namespace   #{'scrapbook.core}}
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/yourname/scrapbook"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 repl {:init-ns    'scrapbook.core}
 jar {:main        'scrapbook.core
      :file        (str "scrapbook-" version "-standalone.jar")})

(deftask build
  "Build the project locally as a JAR."
  [d dir PATH #{str} "the set of directories to write to (target)."]
  (let [dir (if (seq dir) dir #{"target"})]
    (comp (aot) (pom) (uber) (jar) (target :dir dir))))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (with-pass-thru fs
    (require '[scrapbook.core :as app])
    (let [args {"port" 80}]
      (apply (resolve 'app/-main) args))))

(require '[adzerk.boot-test :refer [test]])
