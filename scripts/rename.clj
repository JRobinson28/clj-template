(ns rename
  "Rename the template project, e.g. `mise run rename my-app`."
  (:require
   [babashka.fs :as fs]
   [babashka.process :as p]
   [clojure.string :as str]))

(def ^:private OLD-NAME "clj-template")
(def ^:private SRC-ROOTS ["src/clj" "test" "dev/src"])
(def ^:private NAME-RE #"[a-z][a-z0-9]*(-[a-z0-9]+)*(\.[a-z][a-z0-9]*(-[a-z0-9]+)*)*")

(defn- munge-name
  "clj-template -> clj_template"
  [s]
  (str/replace s "-" "_"))

(defn- ns->path
  "acme.my-app -> acme/my_app"
  [s]
  (-> s munge-name (str/replace "." "/")))

(defn- tracked-files
  []
  (->> (p/shell {:out :string} "git ls-files")
       :out
       str/split-lines
       (remove #{"scripts/rename.clj"})
       (filter fs/regular-file?)))

(defn- drop-template-section
  "The README's rename instructions are moot once renamed."
  [s]
  (str/replace s #"(?s)\n## Using the template\n.*?(?=\n## |\z)" ""))

(defn- rewrite-files!
  [new-name]
  (doall
   (for [f (tracked-files)
         :let [content (slurp f)
               new-content (cond-> (-> content
                                       (str/replace OLD-NAME new-name)
                                       (str/replace (munge-name OLD-NAME) (munge-name new-name)))
                             (= "README.md" f) drop-template-section)]
         :when (not= content new-content)]
     (do (spit f new-content)
         f))))

(defn- move-dirs!
  [new-name]
  (doall
   (for [root SRC-ROOTS
         :let [from (fs/path root (munge-name OLD-NAME))
               to (fs/path root (ns->path new-name))]
         :when (fs/directory? from)]
     (do (when (fs/exists? to)
           (throw (ex-info (str to " already exists") {})))
         (fs/create-dirs (fs/parent to))
         (fs/move from to)
         (str from " -> " to)))))

(defn -main
  [& args]
  (let [[new-name] args]
    (when-not (and (= 1 (count args)) (re-matches NAME-RE new-name))
      (binding [*out* *err*]
        (println "Usage: mise run rename <name>  (e.g. my-app or acme.my-app)"))
      (System/exit 1))
    (when (= OLD-NAME new-name)
      (println "Already named" OLD-NAME)
      (System/exit 0))
    (let [files (rewrite-files! new-name)
          dirs (move-dirs! new-name)]
      (when (and (empty? files) (empty? dirs))
        (binding [*out* *err*]
          (println "Nothing to rename: no occurrences of" OLD-NAME))
        (System/exit 1))
      (run! #(println "updated" %) files)
      (run! #(println "moved  " %) dirs)
      (println "Renamed" OLD-NAME "->" new-name))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
