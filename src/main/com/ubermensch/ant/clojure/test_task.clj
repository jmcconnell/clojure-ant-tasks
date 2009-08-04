(ns com.ubermensch.ant.clojure.test-task
  (:gen-class
     :name com.ubermensch.ant.clojure.TestTask
     :extends com.ubermensch.ant.clojure.base_task
     :init init-task
     :state state)
  (:use clojure.contrib.test-is)
  (:require [com.ubermensch.ant.clojure.base-task :as base]))

(def failed (ref 0))
(declare orig-report)

(defn- -init-task [] (base/initial-state))

(defn -execute [this]
  (base/with-classloader
    (doseq [an-ns (:namespaces @state)] (require (symbol (.name an-ns))))
    (binding [orig-report report
              report #(do (if (#{:fail :error} (:type %))
                            (dosync (commute failed inc)))
                        (orig-report %))]
      (doseq [an-ns (:namespaces @state)] (run-tests (symbol (.name an-ns))))
      (if (pos? @failed) (throw (org.apache.tools.ant.BuildException.
                                  (str "tests failed: " @failed)))))))
