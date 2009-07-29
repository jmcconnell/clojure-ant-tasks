(ns com.ubermensch.ant.clojure.test-task
  (:gen-class
     :name com.ubermensch.ant.clojure.TestTask
     :extends org.apache.tools.ant.Task
     :init init-task
     :state state
     :methods [[addNamespace [com.ubermensch.ant.clojure.Namespace] void]
               [createClasspath [] org.apache.tools.ant.types.Path]])
  (:use [clojure.contrib test-is])
  (:import [org.apache.tools.ant.types Path]
           [org.apache.tools.ant AntClassLoader]))

(def failed (ref 0))
(declare orig-report)

(defn- -init-task [] [[] (atom {:namespaces []
                                :classpath nil
                                :classloader (clojure.lang.RT/baseLoader)})])

(defmacro with-state [& body]
  `(let [~'state (.state ~'this)]
     ~@body))

(defmacro with-classloader [& body]
  `(with-state
     (binding [*use-context-classloader* true]
       (let [thread# (Thread/currentThread)
             orig-cl# (.getContextClassLoader thread#)
             cl# (AntClassLoader. (:classloader @~'state)
                                  (.getProject ~'this)
                                  (:classpath @~'state))]
         (try
           (.setContextClassLoader (Thread/currentThread) cl#)
           ~@body
           (finally
             (.setContextClassLoader (Thread/currentThread) orig-cl#)))))))

(defn -execute [this]
  (with-classloader
    (doseq [an-ns (:namespaces @state)] (require (symbol (.name an-ns))))
    (binding [orig-report report
              report #(do (if (#{:fail :error} (:type %))
                            (dosync (commute failed inc)))
                        (orig-report %))]
      (doseq [an-ns (:namespaces @state)] (run-tests (symbol (.name an-ns))))
      (if (pos? @failed) (throw (org.apache.tools.ant.BuildException.
                                  (str "tests failed: " @failed)))))))

(defn -addNamespace [this an-ns]
  (with-state
    (swap! state #(assoc % :namespaces (conj (:namespaces @state) an-ns)))))

(defn -createClasspath [this]
  (with-state
    (let [cp (:classpath @state)]
      (if cp
        (.createPath cp)
        (let [cp (Path. (.getProject this))]
          (swap! state #(assoc % :classpath cp))
          cp)))))
