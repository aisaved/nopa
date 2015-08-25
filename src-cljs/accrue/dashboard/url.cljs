(ns accrue.dashboard.url)


(defn entity-url [hash-url entity-id]
  (set! (.-hash js/window.location) 
           (str "#/" hash-url "/" entity-id)))
