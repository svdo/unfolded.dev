(defproject unfolded-dev "0.1.0"
            :description "My personal blog Unfolded.dev"
            :url "https://unfolded.dev"
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.10.3"]
                           [ring/ring-devel "1.9.3"]
                           [compojure "1.6.2"]
                           [ring-server "0.5.0"]
                           [cryogen-flexmark "0.1.4"]
                           [cryogen-core "0.4.0"]]
            :plugins [[lein-ring "0.12.5"]]
            :main cryogen.core
            :ring {:init cryogen.server/init
                   :handler cryogen.server/handler}
            :aliases {"serve"      ["run" "-m" "cryogen.server"]
                      "serve:fast" ["run" "-m" "cryogen.server" "fast"]})
