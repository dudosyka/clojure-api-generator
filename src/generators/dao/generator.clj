(ns generators.dao.generator
  (:require [clojure.string :as string]))

(defn template []
  (slurp "/Users/dudosyka/IdeaProjects/modules-generator/resources/templates/Dao"))

(defn parse-field [module modules field]
  (if (> (count field) 2)
    (let [referenced (get modules (last field))
          referenced-name (:name referenced)]
      (str "var " (first field) " by " referenced-name "Dao referencedOn " (:name module) "Model." (first field) "\n    "
           "var " (first field) "Id by " (:name module) "." (first field)))
    (str "var " (first field) " by " (:name module) "." (first field))))


(defn generate-fields [module modules]
  (string/join "\n    " (map #(parse-field module modules (second %)) (:full-dto module))))

(defn parse-build-item [item]
  (if (> (count item) 2)
    (str "this." (first item) "Id.value,")
    (str "this." (first item) ",")))

(defn generate-builder [module]
  (str "this.id.value,\n        " (string/join "\n        " (map #(parse-build-item (second %)) (:full-dto module)))))

(defn generate-referenced-import [module project]
  (str "import " (:package project) ".modules." (string/lower-case (:name module)) ".data.dao." (:name module) "Dao"))


(defn generate-referenced-imports [module modules project]
  (->> module
       :full-dto
       (filter #(> (count (second %)) 2))
       (map #(generate-referenced-import (get modules (-> % second last)) project))
       (string/join "\n")))


(defn generate-dao [module modules project]
  (let [template (template)
        module-path (str (:package project) ".modules." (string/lower-case (:name module)))
        module-name (:name module)
        replacements {:dao-path     module-path
                      :package-name (:package project)
                      :dto-path     (str module-path ".data.dto." module-name "Dto")
                      :model-path   (str module-path ".data.model." module-name "Model")
                      :model-name-capitalize module-name
                      :class-name   (str module-name "Dao(id: EntityID<Int>) : BaseIntEntity<" module-name "Dto>(id, " module-name "Model)")
                      :companion-object (str "BaseIntEntityClass<" module-name "Dto, " module-name "Dao>(" module-name "Model)")
                      :fields (generate-fields module modules)
                      :builder (generate-builder module)
                      :referenced-dao-import (generate-referenced-imports module modules project)}]
    (string/replace template #"\{\{:(.+)\}\}"
                    (fn [[_ key]]
                      (get replacements (keyword key) "")))))

(defn generate [module modules project]
  [(str (string/lower-case (:name module)) "/data/dao/")
   [(str (:name module) "Dao.kt")
    (generate-dao module modules project)]])