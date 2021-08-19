(ns igniteinator.text
  (:require [igniteinator.state :refer [language]]
            [clojure.string :as s]))

(def strings
  {
   :subtitle                  {:en "an unoffical app for the board game"}
   :ok                        {:en "ok"}
   :back                      {:en "back"}
   :empty-list                {:en "empty list"}
   :select-all                {:en "select all"}
   :search                    {:en "search"}
   :using                     {:en "using"}
   :regular-expressions       {:en "regular expressions"}
   :clear-selection           {:en "clear selection"}
   :data-load-error           {:en "error loading data"}
   :show-combos               {:en "show known combos"}
   :no-combos                 {:en "no known combos for this card"}
   :no-more-combos            {:en "no more known combos for this card"}
   :cards-page-title          {:en "cards"}
   :combos-page-title         {:en "combos for"}
   :card-selection            {:en "card selection"}
   :select-all-button         {:en "all"}
   :select-some-button        {:en "select cards"}
   :select-cards-dialog-title {:en "select cards"}
   :install-app               {:en "Install app"}
   :add-to-home-screen        {:en "Add to Home screen"}
   :a2hs-instructions-title   {:en "Add to Home screen: Instructions"}
   :got-it                    {:en "Got it!"}
   ;; A2HS Instructions for iPhone. Cf. https://www.netguru.com/blog/pwa-ios.
   ;; We cannot show the icon because of Apple copyright.
   ;; (https://developer.apple.com/design/human-interface-guidelines/sf-symbols/overview/)
   :a2hs-instructions-ios     {:en "Click the \"Share\" icon, then find \"Add to Home Screen\"."}
   })

(defn txt [s]
  (get-in strings [s @language]))

(defn txt-c [s]
  (s/capitalize (txt s)))
