(ns com.ubermensch.ant.clojure.namespace
  (:gen-class
     :name com.ubermensch.ant.clojure.Namespace
     :init init
     :state state
     :methods [[addText [String] void]
               [toString [] void]]))

(defn- -init [] [[] (atom {})])

(defn -addText [this text] (swap! (.state this) #(assoc % :ns text)))

(defn -toString [this] (str @(.state this)))
