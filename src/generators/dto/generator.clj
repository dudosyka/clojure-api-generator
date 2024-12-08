(ns generators.dto.generator
  (:require [clojure.string :as string]
            [generators.model.fields.field :as field]))

(defn template []
  (slurp "/Users/dudosyka/IdeaProjects/modules-generator/resources/templates/Dto"))

(defn generate-field [module modules field]
  (if (> (count field) 1)
    (let [nested (get modules (first field))]
      (if (> (count field) 2)
        (generate-field (get modules (first field)) modules (drop 1 field))
        (let [field (-> nested :full-dto (get (second field)))]
          [(str (:name nested) (string/capitalize (first field))) (second field)])))
    (if (= (first field) :id)
      ["id" :int]
      (-> module :full-dto (get (first field))))))

(defn build-list-dto-fields [module modules]
  (->> module :list-dto
       (map #(generate-field module modules %))
       (map (fn [v] (into [:dto] (reverse v))))
       (map #(apply field/generate (conj % false)))
       (string/join "\n")))

(defn generate-list-dto [module modules project]
  (let [template (template)
        fields (build-list-dto-fields module modules)
        dto-name (str (:name module) "List")
        dto-path (str (:package project) ".modules." (string/lower-case (:name module)))
        replacements {:fields fields
                      :dto-name dto-name
                      :dto-path dto-path}]
    (string/replace template #"\{\{:(.+)\}\}"
                    (fn [[_ key]]
                      (get replacements (keyword key) "")))))

(defn generate-dto [module project nullable add-id prefix]
  (let [template (template)
        fields (->> module :full-dto
                    (map (fn [[_ v]] [:dto (second v) (first v) nullable]))
                    (map #(apply field/generate %))
                    (reduce #(str %1 "\n" %2) (if add-id "val id: Int," "")))
        dto-name (str prefix (:name module))
        dto-path (str (:package project) ".modules." (string/lower-case dto-name))
        replacements {:fields   fields
                      :dto-name dto-name
                      :dto-path dto-path}]
    (string/replace template #"\{\{:(.+)\}\}"
                    (fn [[_ key]]
                      (get replacements (keyword key) "")))))

(defn generate-full-dto [module project]
  (generate-dto module project false true ""))

(defn generate-create-dto [module project]
  (generate-dto module project false false "Create"))

(defn generate-update-dto [module project]
  (generate-dto module project true false "Update"))

(defn generate [module modules project]
  (let [dtos [(str (string/lower-case (:name module)) "/data/dto/")
              [(str (:name module) "Dto.kt")
               (generate-full-dto module project)

               (str "Create" (:name module) "Dto.kt")
               (generate-create-dto module project)

               (str "Update" (:name module) "Dto.kt")
               (generate-update-dto module project)]]
        list-dto-empty (zero? (count (:list-dto module)))]
    (if list-dto-empty
      dtos
      (let [[dir dtos] dtos]
        [dir (-> dtos
                 (conj [(str (:name module) "ListDto.kt")
                        (generate-list-dto module modules project)])
                 flatten)]))))
