(ns scrapbook.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [rum.core :refer [defc render-static-markup]]
            [config.core :refer [env]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.session-timeout :refer [wrap-idle-session-timeout]]
            [jdbc-ring-session.core :refer [jdbc-store]]
            [jdbc-ring-session.cleaner :refer [start-cleaner]]
            [migratus.core :as migratus])
  (:gen-class))

(defc html-frame []
  [:html
   [:head
    [:title "A Scrapbook"]]
   [:body
    [:h1 {:id "main-headline"} 
     "Welcome to Scrapbook, Stranger!"]
    [:div {:id "min-content"} 
     "We hope you will like it here"]]])

(defn app-handler [request]
  (let [{session :session} request]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (render-static-markup (html-frame))
     :session (assoc session :username "Tim")}))

(defn init-db [db-conf]
  (migratus/migrate {:store                :database
                     :migration-dir        "migrations/"
                     :init-in-transaction? false
                     :migration-table-name "migratus_table"
                     :db db-conf}))

(defn -main
  "Starts the web server and the application"
  [& args]
  (let [db-conf (:db env)
        session-store (jdbc-store db-conf)
        port (:port env)
        session-expiry-in-minutes 5]
    (init-db db-conf)
    (start-cleaner db-conf)
    (run-jetty 
     (-> app-handler
         (wrap-idle-session-timeout {:timeout (* session-expiry-in-minutes 60)
                                     :timeout-response {:status 200
                                                        :body "Session timed out"}})
         (wrap-defaults (-> site-defaults 
                            (assoc-in [:session :store] session-store)))) 
     {:port port})))
