(ns com.ubermensch.ant.clojure.test-task
  (:gen-class
     :name com.ubermensch.ant.clojure.TestTask
     :extends com.ubermensch.ant.clojure.base_task
     :init init-task
     :state state)
  (:use [clojure.test :only [report run-tests]])
  (:require [com.ubermensch.ant.clojure.base-task :as base]
            [clojure.set :as set]))

(def failed (ref 0))
(declare ^:dynamic orig-report)

(defn- -init-task [] (base/initial-state))

(defn -execute [this]
  (base/with-classloader
    (let [namespaces (set (set/union
                            (map #(.name %) (:namespaces @state))
                            (base/filesets->namespaces (:filesets @state))))]
      (doseq [an-ns namespaces] (require (symbol an-ns)))
      (binding [orig-report report
                report #(do (if (#{:fail :error} (:type %))
                              (dosync (commute failed inc)))
                          (orig-report %))]
        (doseq [an-ns namespaces] (run-tests (symbol an-ns)))
        (if (pos? @failed) (throw (org.apache.tools.ant.BuildException.
                                    (str "tests failed: " @failed))))))))
