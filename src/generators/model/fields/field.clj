(ns generators.model.fields.field)

(defmulti generate (fn [c t _ _] [c t]))


(defmethod generate [:model :string] [_ _ field _]
  [(str "    val " (first field) " = text(\"" (first field) "\")")])

(defmethod generate [:dto :string] [_ _ field nullable]
  (str "    val " field ":String" (if nullable "?" "") ","))


(defmethod generate [:model :timestamp] [_ _ field _]
  [(str "    val " (first field) " = long(\"" (first field) "\")")])

(defmethod generate [:dto :timestamp] [_ _ field nullable]
  (str "    val " field ":Long" (if nullable "?" "") ","))


(defmethod generate [:model :relation] [_ _ relation _]
  (let [name (:field-name relation)
        model-name (:model-name relation)
        on-delete (:on-delete relation)
        on-update (:on-update relation)]
    [(str "import " (:path relation))
     (str "    val " name " = reference("
          "\"" name "\", "
          model-name "Model, "
          on-delete ", "
          on-update
          ")")]))

(defmethod generate [:dto :relation] [_ _ field nullable]
  (str "    val " field ":Int" (if nullable "?" "") ","))


(defmethod generate [:model :int] [_ _ field _]
  [(str "    val " (first field) " = integer(\"" (first field) "\")")])

(defmethod generate [:dto :int] [_ _ field nullable]
  (str "    val " field ":Int" (if nullable "?" "") ","))


(defmethod generate [:model :double] [_ _ field _]
  [(str "    val " (first field) " = double(\"" (first field) "\")")])

(defmethod generate [:dto :double] [_ _ field nullable]
  (str "    val " field ":Double" (if nullable "?" "") ","))


(defmethod generate [:model :bool] [_ _ field _]
  [(str "    val " (first field) " = bool(\"" (first field) "\")")])

(defmethod generate [:dto :bool] [_ _ field nullable]
  (str "    val " field ":Boolean" (if nullable "?" "") ","))