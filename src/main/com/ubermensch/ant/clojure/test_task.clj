(ns com.ubermensch.ant.clojure.test-task
  (:gen-class
     :name com.ubermensch.ant.clojure.TestTask
     :extends org.apache.tools.ant.Task
     :init init-task
     :state state
     :methods [[addNamespace [com.ubermensch.ant.clojure.Namespace] void]]))

(defn- -init-task [] [[] (atom {:namespaces []})])

(defn -execute [this]
  (println @(.state this)))

(defn -addNamespace [this an-ns]
  (let [state (.state this)]
    (swap! state #(assoc % :namespaces (conj (:namespaces @state) an-ns)))))
