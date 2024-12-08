(ns core
  (:gen-class)
  (:require [clojure.edn :as edn])
  (:require [generators.model.generator :as model])
  (:require [generators.controller.generator :as controller])
  (:require [generators.service.generator :as service])
  (:require [generators.dao.generator :as dao])
  (:require [clojure.string :as string]
            [generators.dto.generator :as dto])
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs])
  (:import (java.io File)))

(defn read-conf [path]
  (let [edn-content (slurp path)]
    (edn/read-string edn-content)))

(defn get-modules [conf]
  (mapv identity (partition 2 (:modules conf))))

(defn get-ordering [conf]
  (mapv first (partition 2 (:modules conf))))

(defn write-dir [dir modules-dir]
  (reduce (fn [acc v]
            (.mkdir (File. (str modules-dir (str acc "/" v))))
            (str acc "/" v)) "" (string/split (first dir) #"/"))
  (doseq [file (partition 2 (second dir))]
    (spit (str modules-dir (first dir) (first file)) (second file))))

(defn replace-in-file [file old-text new-text]
  (let [content (slurp file)
        updated-content (string/replace content old-text new-text)]
    (spit file updated-content)))

(defn process-directory [dir old-text new-text]
  (doseq [file (file-seq (io/file dir))
          :when (.isFile file)]
    (replace-in-file file old-text new-text)))

(defn parse [conf-path output]
  (when (fs/file? output)
    (throw (ex-info "Bad output path" {})))
  (when (fs/exists? output)
    (fs/delete-dir (str output "/proj")))
  (let [conf (read-conf conf-path)
        project (:project conf)
        modules (->> conf
                     (get-modules)
                     (map #(into [] %))
                     (into {}))
        kotlin-path (str output "/proj/src/main/kotlin/")
        output (fs/copy-dir (io/resource "base") (str output "/proj"))
        main-package-path (str kotlin-path (:package project))
        modules-dir (str main-package-path "/modules/")]
    (fs/rename (str kotlin-path "philarmonic") main-package-path)
    (process-directory output #"philarmonic" (:package project))
    (doseq [module modules]
      (let [controller-files (controller/generate (second module) project)
            service-files (service/generate (second module) modules project)
            model-files (model/generate (second module) modules project)
            dao-files (dao/generate (second module) modules project)
            dto-files (dto/generate (second module) modules project)
            dirs (-> [controller-files]
                     (conj service-files model-files dao-files dto-files))]
        (doseq [dir dirs]
          (write-dir dir modules-dir))))))

(defn -main [& args]
  (if (= (count args) 2)
    (parse (fs/file (first args)) (fs/file (second args)))
    (println "Error. \n Usage: app <path-to-conf.edn> <path-to-output-dir>")))