(ns NmlFilter.core
  (:gen-class)
  (:require [clojure.xml :as xml])
  (:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter.xml :as zf])
  (:require [clojure.contrib.string :as str]))


(defn titles [zipper]
  (zf/xml-> zipper :COLLECTION :ENTRY (zf/attr :TITLE)))

(defn artists [zipper]
  (zf/xml-> zipper :COLLECTION :ENTRY (zf/attr :ARTIST)))

(defn normalize [title]
  (let [[name remix] (str/split #" - " title)]
    (if (or (not remix)
	    (= (compare remix "Original Mix") 0))
      name
      (format "%s (%s)" name remix))))

(defn tracklist [zipper]
  (for [[index title artist]
	(map vector
	     (range)
	     (titles zipper)
	     (artists zipper))]
    (format "%d. %s - %s" (+ index 1) artist (normalize title))))

(defn pretty-print [nml]
  (doseq [track (tracklist (zip/xml-zip (xml/parse nml)))]
    (println track)))

(defn -main [& args]
  (if args
    (pretty-print (first args))
    (println "No file path provided.")))