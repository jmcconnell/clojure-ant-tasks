(ns com.ubermensch.ant.clojure.compile-task
  (:gen-class
     :name com.ubermensch.ant.clojure.CompileTask
     :extends com.ubermensch.ant.clojure.base_task
     :init init-task
     :state state)
  (:require [com.ubermensch.ant.clojure.base-task :as base]))

(defn- -init-task [] (base/initial-state))

(defn -execute [this]
  (base/with-classloader
    (try
      (binding [*compile-path* "classes"]
        (println "compiling to" *compile-path*)
        (doseq [an-ns (:namespaces @state)]
          (do
            (println "  compiling" (.name an-ns))
            (compile (symbol (.name an-ns))))))
      (catch Exception e
        (throw (org.apache.tools.ant.BuildException.
                 (str "compilation failed: " (.getMessage e))))))))
