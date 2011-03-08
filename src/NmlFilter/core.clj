(ns NmlFilter.core
  (:gen-class)
  (:require [clojure.xml :as xml])
  (:require [clojure.zip :as zip])
  (:require [clojure.contrib.zip-filter.xml :as zfx])
  (:require [clojure.contrib.zip-filter :as zf])
  (:require [clojure.contrib.string :as str]))


(defn titles [zipper]
  (zfx/xml-> zipper :COLLECTION :ENTRY (zfx/attr :TITLE)))

(defn artists [zipper]
  (zfx/xml-> zipper :COLLECTION :ENTRY (zfx/attr :ARTIST)))

(defn primary-keys [zipper]
  (for [[volume dir file]
	(map vector
	(zfx/xml-> zipper :COLLECTION :ENTRY :LOCATION (zfx/attr :VOLUME))
	(zfx/xml-> zipper :COLLECTION :ENTRY :LOCATION (zfx/attr :DIR))
	(zfx/xml-> zipper :COLLECTION :ENTRY :LOCATION (zfx/attr :FILE)))]
    (str volume dir file)))

(defn entries [zipper]
  (zipmap
   (primary-keys zipper)
   (map vector (artists zipper) (titles zipper))))

(defn playlist [zipper]
  (zfx/xml-> zipper
	    :PLAYLISTS
	    :NODE
	    :SUBNODES
	    :NODE
	    :PLAYLIST
	    :ENTRY
	    :PRIMARYKEY
	    (zfx/attr :KEY)))

(defn ordered-entries [zipper]
  (for [key (playlist zipper)]
    (find (entries zipper) key)))

(defn normalize [title]
  (let [[name remix] (str/split #" - " title)]
    (if (or (not remix)
	    (= remix "Original Mix"))
      name
      (format "%s (%s)" name remix))))

(defn tracklist [zipper]
  (for [[index entry] (map vector (range) (vals (ordered-entries zipper)))]
    (let [[artist title] entry]
      (format "%d. %s - %s" (+ index 1) artist (normalize title)))))

(defn pretty-print [nml]
  (doseq [track (tracklist (zip/xml-zip (xml/parse nml)))]
    (println track)))

(defn -main [& args]
  (if args
    (pretty-print (first args))
    (println "No file path provided.")))