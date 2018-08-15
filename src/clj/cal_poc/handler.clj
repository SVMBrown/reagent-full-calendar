(ns cal-poc.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [cal-poc.middleware :refer [wrap-middleware]]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-js "/fullcalendar-3.9.0/lib/jquery.min.js")
   (include-js "/fullcalendar-3.9.0/lib/jquery-ui.min.js")
   (include-js "/fullcalendar-3.9.0/lib/moment.min.js")
   (include-js "/fullcalendar-3.9.0/fullcalendar.min.js")
   (include-css "/fullcalendar-3.9.0/fullcalendar.min.css")
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))


(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))
  
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
