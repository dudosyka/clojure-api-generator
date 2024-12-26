(ns generators.service.generator
  (:require [clojure.string :as string]))

(defn template []
  (slurp "/Users/dudosyka/IdeaProjects/modules-generator/resources/templates/Service"))

(defn parse-field-on-create [modules field]
  (if (> (count field) 2)
    (str "this." (first field) " = " (-> modules (get (last field)) :name) "Dao[createDto." (first field) "Id]")
    (str "this." (first field) " = createDto." (first field))))


(defn generate-create-fields [module modules]
  (->> module :full-dto
       (map #(parse-field-on-create modules (second %)))
       (string/join "\n            ")))

(defn parse-field-on-update [modules field]
  (if (> (count field) 2)
    (str "dao." (first field)
         " = if (updateDto." (first field) "Id == null) dao." (first field)
         " else " (-> modules (get (last field)) :name) "Dao[createDto." (first field) "Id]")
    (str "dao." (first field) " = updateDto." (first field) " ?: dao." (first field))))

(defn generate-update-fields [module modules]
  (->> module :full-dto
       (map #(parse-field-on-update modules (second %)))
       (string/join "\n        ")))

(defn generate-joins [module modules]
  (->> module :list-dto
       (filter #(> (count %) 1))
       (map #(->> %
                  (drop-last 1)
                  (map (fn [model] (let [join-model-name (-> modules (get model) :name)]
                                     (str ".leftJoin(" join-model-name "Model)"))))))
       flatten
       distinct
       (string/join "\n            ")
       (str "\n            ")))

(defn generate-builder-item [module modules field]
  (if (> (count field) 2)
    (generate-builder-item module modules (drop 1 field))
    (if (= (count field) 1)
      [(str (:name module) "Model")
       (-> module :full-dto (get (first field)) first)]
      [(str (-> modules (get (first field)) :name) "Model")
       (-> modules (get (first field)) :full-dto (get (last field)) first)])))

(defn generate-list-builder [model module modules]
  (->> module :list-dto
       (map #(let [[model-name field-name] (generate-builder-item module modules %)]
               (if (= field-name nil) nil
                 (str "it[" model-name "." field-name "]"))))
       (filter some?)
       (string/join ",\n                    ")
       (str "\n                    it[" model ".id].value,\n                    ")))

(defn generate-get-all [module modules]
  (let [n-split "\n            "
        nn-split "\n                "
        model (str (:name module) "Model")
        dto (str (:name module) "ListDto")
        joins (generate-joins module modules)
        list-builder (generate-list-builder model module modules)]
    (str model joins n-split ".selectAll().map { "
         nn-split dto "("
         list-builder
         nn-split ")" n-split "}")))

(defn generate-import-pair [module package-name]
  (let [prefix (str "import " package-name ".modules." (string/lower-case (:name module)) ".data.")]
    (str prefix "model." (:name module) "Model\n"
         prefix "dao." (:name module) "Dao")))

(defn generate-additional-imports [module module-key modules package-name]
  (let [full-dto-related (->> module :full-dto
                              (map second)
                              (filter #(> (count %) 2))
                              (map last))
        list-dto-related (->> module :list-dto
                              (filter #(> (count %) 2))
                              (map #(drop-last 1 %))
                              flatten)
        all-related (-> full-dto-related
                        (conj list-dto-related)
                        (conj [module-key])
                        flatten
                        distinct)]
    (->> all-related
         (map #(generate-import-pair (get modules %) package-name))
         (string/join "\n"))))


(defn generate-service [module-key modules project]
  (let [module (get modules module-key)
        template (template)
        package-name (:package project)
        module-path (str package-name ".modules." (string/lower-case (:name module)))
        replacements {:service-path module-path
                      :package-name package-name
                      :create-dto-path (str module-path ".data.dto.Create" (:name module) "Dto")
                      :update-dto-path (str module-path ".data.dto.Update" (:name module) "Dto")
                      :dto-path (str module-path ".data.dto." (:name module) "Dto")
                      :list-dto-path (str module-path ".data.dto." (:name module) "ListDto")
                      :model-name-capitalize (:name module)
                      :function-type (str (:name module) "Dto): " (:name module) "Dto")
                      :create-fields (generate-create-fields module modules)
                      :get-all (generate-get-all module modules)
                      :update-fields (generate-update-fields module modules)
                      :dao-model-imports (generate-additional-imports module module-key modules (:package project))
                      :position-query-prefix (str (:name module) "Dao.find { " (:name module))}]
    (string/replace template #"\{\{:(.+)\}\}"
                    (fn [[_ key]]
                      (get replacements (keyword key) "")))))

(defn generate [module-key module modules project]
  [(str (string/lower-case (:name module)) "/service/")
   [(str (:name module) "Service.kt")
    (generate-service module-key modules project)]])