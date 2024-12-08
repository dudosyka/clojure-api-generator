(ns generators.model.generator
  (:require [generators.model.fields.field :as field]
            [clojure.string :as string]))

(defn template []
  (slurp "/Users/dudosyka/IdeaProjects/modules-generator/resources/templates/Model"))

(defn generate-model-path [model-name project]
  (str (:package project) ".modules." (string/lower-case model-name) ".data.model." model-name "Model"))

(defn build-relation-data [field-name relation_name modules project]
  (let [related (get modules relation_name)
        model-name (:name related)
        path (str (generate-model-path model-name project))
        on-delete "ReferenceOption.CASCADE"
        on-update "ReferenceOption.CASCADE"]
    {:field-name field-name
     :model-name model-name
     :path path
     :on-delete on-delete
     :on-update on-update}))

(defn generate-fields [model modules project]
  (->> (:full-dto model)
       (map (fn [[k v]]
              [:model (second v) (if (> (count v) 2)
                                   (build-relation-data (first v) (last v) modules project)
                                   v) false]))
       (map #(apply field/generate %))))

(defn merge-fields [fields]
  (reduce (fn [acc field] (if (> (count field) 1)
                            [(str (first acc) "\n" (first field))
                             (str (second acc) "\n" (second field))]
                            [(first acc)
                             (str (second acc) "\n" (first field))])) ["" ""] fields))

(defn generate-model [model modules project]
  (let [template (template)
        fields (-> (generate-fields model modules project)
                   (merge-fields))
        header (first fields)
        fields (last fields)
        replacements {:package-name (:package project)
                      :header header
                      :model-name (:name model)
                      :model-path (str (:package project) ".modules." (string/lower-case (:name model)))
                      :fields fields}]
    (string/replace template #"\{\{:(.+)\}\}"
                 (fn [[_ key]]
                   (get replacements (keyword key) "")))))

(defn generate [module modules project]
  [(str (string/lower-case (:name module)) "/data/model/")
   [(str (:name module) "Model.kt")
    (generate-model module modules project)]])