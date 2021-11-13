(ns igniteinator.text
  (:require [igniteinator.util.re-frame :refer [<sub]]
            [igniteinator.util.string :as ss]
            [clojure.string :as s]))

(def strings
  {
   :subtitle                    {:en "an inator for"}
   :ok                          {:en "ok"}
   :cancel                      {:en "cancel"}
   :close                       {:en "close"}
   :back                        {:en "back"}
   :some-of                     {:en "some of"}
   :all-of                      {:en "all of"}
   :copy                        {:en "copy"}
   :link                        {:en "link"}
   :names                       {:en "names"}
   :empty-list                  {:en "empty list"}
   :select-all                  {:en "select all"}
   :search                      {:en "search"}
   :using                       {:en "using"}
   :combos                      {:en "combos"}
   :reload                      {:en "reload"}
   :regular-expressions         {:en "regular expressions"}
   :clear-selection             {:en "clear selection"}
   :data-load-error             {:en "error loading data"}
   :card-tooltip-combos         {:en "Show card and known combos"}
   :card-tooltip-no-combos      {:en "Show card (no known combos for this card)"}
   :card-tooltip-no-more-combos {:en "Show card (no more known combos for this card)"}
   :no-combos                   {:en "No known combos for this card."}
   :cards-page-title            {:en "Cards"}
   :card-details-page-title     {:en ""}
   :combos-title                {:en "Combos"}
   :card-selection              {:en "card selection"}
   :select-all-button           {:en "all"}
   :select-some-button          {:en "select cards"}
   :select-cards-dialog-title   {:en "Select cards"}
   :combos-dialog-title         {:en "Cards with known combos"}
   :combos-dialog-official-help {:en "Other combos are calculated from this set."}
   :combos-dialog-official-item {:en "Official combos list"}
   :combos-dialog-all-item      {:en "All cards with known combos"}
   :setups-page-title           {:en "Suggested setups"}
   :required-boxes              {:en "Required boxes"}
   :copy-to-cards-page-tooltip  {:en "Copy setup to Cards page"}
   :copy-to-cards-page-button   {:en "Cards page"}          ; preceded by icon
   :app-update-message          {:en "New version available!"}
   :app-update-button           {:en "Use now"}
   :caching-progress-title      {:en "Downloading images"}
   :share/button-tooltip        {:en "Share card list"}
   :share/dialog-title          {:en "Share card list"}
   :share/dialog-text           {:en "Copy the following URL or list of names to share the currently listed cards."}
   :share/snackbar-text         {:en "{value} copied to clipboard!"}
   :language-button-tooltip     {:en "Language"}
   :settings-button-tooltip     {:en "Settings"}
   :install-app                 {:en "Install app"}
   :add-to-home-screen          {:en "Add to Home screen"}
   :a2hs-instructions-title     {:en "Add to Home screen: Instructions"}
   :got-it                      {:en "Got it!"}
   :clear-data/button-text      {:en "Clear data …"}
   :clear-data/dialog-title     {:en "Clear data"}
   :clear-data/dialog-text      {:en "Do you want to clear all data and reload the page?"}
   ;; A2HS Instructions for iPhone. Cf. https://www.netguru.com/blog/pwa-ios.
   ;; We cannot show the icon because of Apple copyright.
   ;; (https://developer.apple.com/design/human-interface-guidelines/sf-symbols/overview/)
   :a2hs-instructions-ios       {:en "Click the \"Share\" icon, then find \"Add to Home Screen\"."}
   })

;; Convenience functions to minimize the boilerplate retrieving strings.
;; Use txt-c when the key does not instruct it's specific context and capitalizing is intended.

(defn txt
  ([s]
   (<sub :txt s))
  ([s format-args]
   (ss/format (txt s) format-args)))

(defn txt-c [s]
  (<sub :txt-c s))
