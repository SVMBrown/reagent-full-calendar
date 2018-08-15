(ns cal-poc.core
  (:require [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [cljs.pprint :as pprint]))


(defn $ [this]
  (-> this
      reagent/dom-node
      js/$))

(def events (atom (vec (repeatedly
                        10
                        (fn []
                          (let [start (+ (- (* 1000 60 60 24 30) (rand-int (* 1000 60 60 24 60))) (.valueOf (.now js/Date)))
                                end  (+ start (rand-int (* 1000 60 60 12)))
                                id (random-uuid)]
                            {:id id
                             :title (str id)
                             :start (js/moment start)
                             :end (js/moment end)}))))))

(defn events-fn [cal-start cal-end timezone callback]
  (callback (clj->js
             (filterv
              (fn [{:keys [start end]}]
                (or (< (.valueOf cal-start) start (.valueOf cal-end))
                    (< (.valueOf cal-start) end (.valueOf cal-end))))
              @events))))

(def cal-opts
  {:events events-fn
   :defaultView "agendaWeek"
   :editable true
   :eventDrop (fn [event delta revert parent-ev ui-obj view]
                (let [event (js->clj event :keywordize-keys true)]
                  (swap! events #(mapv (fn [{:keys [id] :as e}] (if (= id (:id event)) event e)) %))))
   :eventResize (fn [event delta revert parent-ev ui-obj view]
                  (let [event (js->clj event :keywordize-keys true)]
                    (swap! events #(mapv (fn [{:keys [id] :as e}] (if (= id (:id event)) event e)) %))))
   :dayClick (fn [date parent-ev view]
               (println "Clicked: " (.format date)))})

;; -------------------------
;; Components

(defn calendar [opts]
  (let [opts (clj->js (or opts {}))]
    (reagent/create-class
     {:display-name "calendar"
      :component-did-mount (fn [this]
                             (-> this
                                 $
                                 (.fullCalendar opts)))
      :component-will-unmount (fn [this]
                                (-> this
                                    $
                                    (.fullCalendar "destroy")))
      :reagent-render
      (fn [_]
        [:div.calendar])})))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to cal-poc"]
   [calendar cal-opts #_{:events events-fn}]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About cal-poc"]
   [:div [:a {:href "/"} "go to the home page"]]])

;; -------------------------
;; Routes

(defonce page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
