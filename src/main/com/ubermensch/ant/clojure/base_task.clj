(ns com.ubermensch.ant.clojure.base-task
  (:gen-class
     :extends org.apache.tools.ant.Task
     :methods [[addNamespace [com.ubermensch.ant.clojure.Namespace] void]
               [createClasspath [] org.apache.tools.ant.types.Path]
               [addFileSet [org.apache.tools.ant.types.FileSet] void]])
  (:use [clojure.contrib.seq-utils :only [flatten]])
  (:import [org.apache.tools.ant.types Path FileSet]
           [org.apache.tools.ant AntClassLoader]))

(defn initial-state
  ([] (initial-state {}))
  ([m] [[] (atom (merge {:namespaces []
                         :filesets []
                         :classpath nil
                         :classloader (clojure.lang.RT/baseLoader)}
                        m))]))

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

(defn- conj-state-value [this k v]
  (with-state
    (swap! state #(assoc % k (conj (k @state) v)))))

(defn -addNamespace [this an-ns]
  (conj-state-value this :namespaces an-ns))

(defn -createClasspath [this]
  (with-state
    (let [cp (:classpath @state)]
      (if cp
        (.createPath cp)
        (let [cp (Path. (.getProject this))]
          (swap! state #(assoc % :classpath cp))
          cp)))))

(defn -addFileSet [this fileset]
  (conj-state-value this :filesets fileset))

(defn- filename->namespace [filename]
  (get (re-matches #"(.*)\.clj"
                   (.. filename (replace \_ \-) (replace \/ \.)))
       1))

(defn- filename-seq [fileset]
  (map #(.getName %) (iterator-seq (.iterator fileset))))

(defn filesets->namespaces [filesets]
  (flatten (map #(map filename->namespace (filename-seq %))
                filesets)))
