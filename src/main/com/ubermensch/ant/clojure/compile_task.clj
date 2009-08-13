(ns com.ubermensch.ant.clojure.compile-task
  (:gen-class
     :name com.ubermensch.ant.clojure.CompileTask
     :extends com.ubermensch.ant.clojure.base_task
     :methods [[setCompilePath [String] void]]
     :init init-task
     :state state)
  (:require [com.ubermensch.ant.clojure.base-task :as base])
  (:import java.io.File))

(defn- -init-task [] (base/initial-state {:compile-path "classes"}))

(defn -setCompilePath [this path]
  (base/with-state
    (swap! state #(assoc % :compile-path path))))

(defn- ns->path [an-ns]
  (.. an-ns (replace \- \_) (replace \. \/)))

(defn- class-file-name [an-ns]
  (str (ns->path an-ns) clojure.lang.RT/LOADER_SUFFIX ".class"))

(defn- last-modified [file]
  (.lastModified (File. (.toURI file))))

(defn- class-file-is-old? [an-ns]
  (let [cl (.getContextClassLoader (Thread/currentThread))
        class-file (.getResource cl (class-file-name an-ns))
        source-file (.getResource cl (str (ns->path an-ns) ".clj"))]
    (if class-file
      ; a class file exists, let's test its age
      (< (last-modified class-file) (last-modified source-file))
      ; there is no class file, so we'll consider that too old :)
      true)))

(defn- should-be-compiled? [an-ns]
  (class-file-is-old? an-ns))

(defn- compile-ns [an-ns]
  (println "  compiling" an-ns)
  (compile (symbol an-ns)))

(defn -execute [this]
  (base/with-classloader
    (try
      (binding [*compile-path* (:compile-path @state)]
        (println "compiling to" *compile-path*)
        (doseq [an-ns (filter #(should-be-compiled? (.name %))
                              (:namespaces @state))]
          (compile-ns (.name an-ns)))
        (doseq [an-ns (filter should-be-compiled?
                              (base/filesets->namespaces (:filesets @state)))]
          (compile-ns an-ns)))
      (catch Exception e
        (throw (org.apache.tools.ant.BuildException.
                 (str "compilation failed: " (.getMessage e))))))))
