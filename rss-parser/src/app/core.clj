(ns app.core
  (:gen-class)
  (:require [clj-http.client :as cli]
            [clojure.data.csv :as csv]
            [clojure.xml :as xml]
            [clojure.java.io :as io])
  (:import java.io.ByteArrayInputStream))

;; - http://b.hatena.ne.jp/hotentry/{category}.rss
;; - all, general, social, economics, life, knowledge, it, fun, entertainment, game

(defn str->input-stream [s]
  (ByteArrayInputStream. (.getBytes s)))

(defn get-hotentry [category]
  (cli/get (str "http://b.hatena.ne.jp/hotentry/" category ".rss")))

(def parsed (xml/parse (str->input-stream (:body (get-hotentry "it")))))

(def filterd (filter #(= :item (:tag %)) (get-in parsed [:content])))

(def item (first filterd))

(defn extract [item]
  (reduce-kv #(assoc %1 %2 (get-in %3 [:content 0]))
             {}
             (select-keys (zipmap (map :tag (:content item))
                                  (:content item))
                          [:title :link :dc:date])))

(with-open [w (io/writer (io/file "/tmp/it.csv"))]
  (csv/write-csv w (map (juxt :title :link :dc:date)
                        (take 2 (map extract filterd)))
                 :quote? (constantly true)))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
