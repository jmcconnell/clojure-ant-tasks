(ns com.ubermensch.ant.clojure.base-task
  (:gen-class
     :extends org.apache.tools.ant.Task
     :methods [[addNamespace [com.ubermensch.ant.clojure.Namespace] void]
               [createClasspath [] org.apache.tools.ant.types.Path]])
  (:import [org.apache.tools.ant.types Path]
           [org.apache.tools.ant AntClassLoader]))

(defn initial-state [] [[] (atom {:namespaces []
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
