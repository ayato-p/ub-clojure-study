(ns app.core
  (:gen-class)
  (:require [clj-http.client :as cli]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.string :as str])
  (:import java.io.ByteArrayInputStream))

;; - http://b.hatena.ne.jp/hotentry/{category}.rss
;; - all, general, social, economics, life, knowledge, it, fun, entertainment, game

(defn str->input-stream [^String s]
  (ByteArrayInputStream. (.getBytes s)))

(defn get-hotentry [category]
  (cli/get (str "http://b.hatena.ne.jp/hotentry/" category ".rss")))

(defn extract [item]
  (reduce-kv #(assoc %1 %2 (get-in %3 [:content 0]))
             {}
             (select-keys (zipmap (:content item)
                                  (map :tag (:content item)))
                          [:title :link :dc:date])))

(defn write-csv [filename items]
  (with-open [w (io/writer (io/file (str "/tmp/hotentry/" filename)))]
    (csv/write-csv w items :quote? (constantly true))))

(defn -main [& args]
  (doseq [category ["all" "general" "social" "economics"
                    "life" "knowledge" "it" "fun"
                    "entertainment" "game"]
          :let [filename (str category ".csv")
                items (->> (get-hotentry category)
                           :body
                           str->input-stream
                           xml/parse
                           :content
                           (filter #(= :item (:tag %)))
                           (map extract)
                           (map (juxt :title :link :dc:date)))]]
    (write-csv filename items)))
