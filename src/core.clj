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

(defn build-application-module-item [module-name package-name]
  {:imports (str "import " package-name ".modules." (string/lower-case module-name) ".service." module-name "Service \n"
                 "import " package-name ".modules." (string/lower-case module-name) ".controller." module-name "Controller \n"
                 "import " package-name ".modules." (string/lower-case module-name) ".data.model." module-name "Model")
   :service (str "        bindSingleton { " module-name "Service(it) }")
   :controller (str "        bindSingleton { " module-name "Controller(it) }")
   :model (str "        " module-name "Model,")})


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
        application-path (str kotlin-path (:package project) "/Application.kt")
        output (fs/copy-dir (io/resource "base") (str output "/proj"))
        main-package-path (str kotlin-path (:package project))
        modules-dir (str main-package-path "/modules/")]
    (fs/rename (str kotlin-path "philarmonic") main-package-path)
    (process-directory output #"philarmonic" (:package project))
    (doseq [module modules]
      (let [controller-files (controller/generate (second module) project)
            service-files (service/generate (first module) (second module) modules project)
            model-files (model/generate (second module) modules project)
            dao-files (dao/generate (second module) modules project)
            dto-files (dto/generate (second module) modules project)
            dirs (-> [controller-files]
                     (conj service-files model-files dao-files dto-files))]
        (doseq [dir dirs]
          (write-dir dir modules-dir))))
    (->> modules
        (map #(-> % second :name))
        (map (fn [module] (build-application-module-item module (:package project))))
        (reduce (fn [v acc]
                   (-> acc
                         (assoc :imports (str (:imports acc) "\n" (:imports v)))
                         (assoc :service (str (:service acc) "\n" (:service v)))
                         (assoc :controller (str (:controller acc) "\n" (:controller v)))
                         (assoc :model (str (:model acc) "\n" (:model v))))) {:imports ""
                                                                              :service ""
                                                                              :controller ""
                                                                              :model ""})
        ((fn [items]
            (replace-in-file (fs/file application-path) "{{:imports-list}}" (:imports items))
            (replace-in-file (fs/file application-path) "{{:services-list}}" (:service items))
            (replace-in-file (fs/file application-path) "{{:controllers-list}}" (:controller items))
            (replace-in-file (fs/file application-path) "{{:models-list}}" (:model items)))))))



    ;(->> modules
    ;     ))))

(defn -main [& args]
  (if (= (count args) 2)
    (println (parse (fs/file (first args)) (fs/file (second args))))
    (clojure.pprint/pprint "Error. \n Usage: app <path-to-conf.edn> <path-to-output-dir>")))

(-main "resources/conf.edn" "output/proj")
