(ns generators.controller.generator
  (:require [clojure.string :as string]))

(defn template []
  (slurp "/Users/dudosyka/IdeaProjects/modules-generator/resources/templates/Controller"))

(defn generate-controller [module project]
  (let [template (template)
        controller-path (str (:package project) ".modules." (string/lower-case (:name module)))
        dto-base-path (str controller-path ".dto.")
        replacements {:model-name-capitalize (:name module)
                      :model-name-lower (string/lower-case (:name module))
                      :package-name (:package project)
                      :controller-path controller-path
                      :create-dto-path (str dto-base-path (:name module) "CreateDto")
                      :update-dto-path (str dto-base-path (:name module) "UpdateDto")
                      :list-dto-path (str dto-base-path (:name module) "ListDto")
                      :service-path (str controller-path ".service." (:name module) "Service")
                      :route (:route module)}]
    (string/replace template #"\{\{:(.+)\}\}"
                    (fn [[_ key]]
                      (get replacements (keyword key) "")))))

(defn generate [module project]
  [(str (string/lower-case (:name module)) "/controller/")
   [(str (:name module) "Controller.kt")
    (generate-controller module project)]])
