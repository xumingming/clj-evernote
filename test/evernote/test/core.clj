(ns evernote.test.core
  (:use [evernote.core])
  (:use [clojure.test])
  (:import [com.evernote.edam.type Note Notebook]))

(deftest test-java->clj-note
  (let [java-note (doto (Note.)
                    (.setGuid "guid")
                    (.setTitle "title")
                    (.setContent "content")
                    (.setContentLength 100)
                    (.setActive true)
                    (.setNotebookGuid "notebook-guid")
                    (.setUpdateSequenceNum 10)
                    (.setUpdated 2000))
        clj-note (java->clj java-note)]
    (is (= "guid" (:guid clj-note)))
    (is (= "title" (:title clj-note)))
    (is (= "content" (:content clj-note)))
    (is (= 100 (:content-length clj-note)))
    (is (= true (:active? clj-note)))
    (is (= "notebook-guid" (:notebook-guid clj-note)))
    (is (= 10 (:update-seq-num clj-note)))
    (is (= 2000 (:updated-at clj-note)))))

(deftest test-clj->java-note
  (let [clj-note {:guid "guid"
                  :title "title"
                  :content "content"
                  :content-length 100
                  :active? true
                  :notebook-guid "notebook-guid"
                  :update-seq-num 10
                  :updated-at 2000}
        ^Note java-note (clj->java :note clj-note)]
    (is (= "guid" (.getGuid java-note)))
    (is (= "title" (.getTitle java-note)))
    (is (= "content" (.getContent java-note)))
    (is (= 100 (.getContentLength java-note)))
    (is (= true (.isActive java-note)))
    (is (= "notebook-guid" (.getNotebookGuid java-note)))
    (is (= 10 (.getUpdateSequenceNum java-note)))
    (is (= 2000 (.getUpdated java-note)))))

(deftest test-java->clj-notebook
  (let [java-notebook (doto (Notebook.)
                        (.setName "name")
                        (.setGuid "guid")
                        (.setUpdateSequenceNum 10)
                        (.setDefaultNotebook true)
                        (.setServiceCreated 1000)
                        (.setServiceUpdated 2000))
        clj-notebook (java->clj java-notebook)]
    (is (= "name" (:name clj-notebook)))
    (is (= "guid" (:guid clj-notebook)))
    (is (= 10 (:update-seq-num clj-notebook)))
    (is (= true (:default? clj-notebook)))
    (is (= 1000 (:created-at clj-notebook)))
    (is (= 2000 (:updated-at clj-notebook)))))

(deftest test-clj->java-notebook
  (let [clj-notebook {:name "name"
                      :guid "guid"
                      :update-seq-num 10
                      :default? true
                      :created-at 1000
                      :updated-at 2000}
        ^Notebook java-notebook (clj->java :notebook clj-notebook)]
    (is (= "name" (.getName java-notebook)))
    (is (= "guid" (.getGuid java-notebook)))
    (is (= 10 (.getUpdateSequenceNum java-notebook)))
    (is (= true (.isDefaultNotebook java-notebook)))
    (is (= 1000 (.getServiceCreated java-notebook)))
    (is (= 2000 (.getServiceUpdated java-notebook)))))
