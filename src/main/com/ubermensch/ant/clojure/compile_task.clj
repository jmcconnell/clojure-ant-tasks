(ns com.ubermensch.ant.clojure.compile-task
  (:gen-class
     :name com.ubermensch.ant.clojure.CompileTask
     :extends com.ubermensch.ant.clojure.base_task
     :init init-task
     :state state)
  (:require [com.ubermensch.ant.clojure.base-task :as base])
  (:import java.io.File))

(defn- -init-task [] (base/initial-state))

(defn- class-file-name [an-ns]
  (str an-ns clojure.lang.RT/LOADER_SUFFIX ".class"))

(defn- last-modified [file]
  (.lastModified (File. (.toURI file))))

(defn- class-file-is-old? [an-ns]
  (let [an-ns (.name an-ns)
        cl (.getContextClassLoader (Thread/currentThread))
        class-file (.getResource cl (class-file-name an-ns))
        source-file (.getResource cl (str an-ns ".clj"))]
    (if class-file
      ; a class file exists, let's test its age
      (< (last-modified class-file) (last-modified source-file))
      ; there is no class file, so we'll consider that too old :)
      true)))

(defn- should-be-compiled? [an-ns]
  (class-file-is-old? an-ns))

(defn -execute [this]
  (base/with-classloader
    (try
      (binding [*compile-path* "classes"]
        (println "compiling to" *compile-path*)
        (doseq [an-ns (filter should-be-compiled? (:namespaces @state))]
          (do
            (println "  compiling" (.name an-ns))
            (compile (symbol (.name an-ns))))))
      (catch Exception e
        (throw (org.apache.tools.ant.BuildException.
                 (str "compilation failed: " (.getMessage e))))))))
