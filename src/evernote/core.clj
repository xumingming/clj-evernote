(ns ^{:doc "Evernote Clojure API"
      :author "xumingming"}
  evernote.core
  (:import [java.util List])
  (:import [org.apache.thrift.transport THttpClient])
  (:import [org.apache.thrift.protocol TBinaryProtocol])
  (:import [com.evernote.edam.userstore UserStore$Client])
  (:import [com.evernote.edam.notestore NoteStore$Client NoteFilter NoteList])
  (:import [com.evernote.edam.type Note Notebook])
  (:import [com.evernote.edam.error EDAMUserException]))

(declare java->clj clj->java get-user-store get-note-store find-notes)

(defn create-notebook
  "Creates a notebook with the specified name."
  {:added "0.1"}
  [note-store name]
  (let [java-notebook (doto (Notebook.)
                        (.setName name))
        java-notebook (.createNotebook ^NoteStore$Client (:store note-store)
                                       (:dev-token note-store) java-notebook)]
    (java->clj java-notebook)))

(defn list-notebooks 
  "Lists all the notebooks for the current user."
  {:added "0.1"}
  [note-store]
  (let [^List notebooks (.listNotebooks ^NoteStore$Client (:store note-store)
                                        (:dev-token note-store))
        notebooks (map java->clj notebooks)
        notebooks (vec notebooks)]
    notebooks))

(defn list-notebook-notes 
  "Lists all the notes under the specified notebook."
  {:added "0.1"}
  [note-store notebook-guid]
  (find-notes note-store :notebook-guid notebook-guid))

(defn create-note
  "Creates a note with the specified title and content"
  {:added "0.1"}
  [note-store title content]
  ;; TODO refactor the content format
  (let [content (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">
<en-note>" content "</en-note>")
        java-note (doto (Note.)
                    (.setTitle title)
                    (.setContent content))
        java-note (try
                    (.createNote ^NoteStore$Client (:store note-store)
                                 (:dev-token note-store) java-note)
                    (catch EDAMUserException e
                      (println e)))]
    (java->clj java-note)))

(defn get-note 
  "Gets a note"
  {:added "0.1"}
  [note-store guid & {:keys [with-content]}]
  (let [note (.getNote ^NoteStore$Client (:store note-store)
                       (:dev-token note-store) guid true false false false)]
    note))

(defn get-note-content
  "Gets the content of a specified note(guid)."
  {:added "0.1"}
  [note-store guid]
  (let [content (.getNoteContent ^NoteStore$Client (:store note-store)
                                 (:dev-token note-store) guid)]
    content))

(defn update-note
  "Updates a specified note."
  {:added "0.1"}
  [note-store note]
  (let [note (clj->java :note note)]
    (.updateNote ^NoteStore$Client (:store note-store)
                 (:dev-token note-store) note)))

(defn delete-note
  "Deletes a specified note."
  {:added "0.1"}
  [note-store guid]
  (.deleteNote ^NoteStore$Client (:store note-store)
               (:dev-token note-store) guid))

(defn find-notes
  "Finds notes for the specified condition."
  {:added "0.1"}
  [note-store & {:keys [notebook-guid]}]
  (let [jfilter (doto (NoteFilter.)
                  (.setNotebookGuid notebook-guid))
        ^NoteList note-list (.findNotes ^NoteStore$Client (:store note-store)
                                        (:dev-token note-store) jfilter 0 100)
        notes (.getNotes note-list)
        notes (map java->clj notes)]
    notes))

(defmulti java->clj class)
(defmethod java->clj Notebook
  [^Notebook notebook]
  {:guid (.getGuid notebook)
   :name (.getName notebook)
   :update-seq-num (.getUpdateSequenceNum notebook)
   :default? (.isDefaultNotebook notebook)
   :created-at (.getServiceCreated notebook)
   :updated-at (.getServiceUpdated notebook)})

(defmethod java->clj Note
  [^Note note]
  {:guid (.getGuid note)
   :title (.getTitle note)
   :content (.getContent note)
   :content-length (.getContentLength note)
   :updated-at (.getUpdated note)
   :active? (.isActive note)
   :update-seq-num (.getUpdateSequenceNum note)
   :notebook-guid (.getNotebookGuid note)})


(defmulti clj->java (fn [type & _] type))
(defmethod clj->java :note
  [type note]
  (doto (Note.)
    (.setGuid (:guid note))
    (.setTitle (:title note))
    (.setContent (:content note))
    (.setContentLength (:content-length note))
    (.setUpdated (:updated-at note))
    (.setActive (:active? note))
    (.setUpdateSequenceNum (:update-seq-num note))
    (.setNotebookGuid (:notebook-guid note))))

(defmethod clj->java :notebook
  [type notebook]
  (doto (Notebook.)
    (.setGuid (:guid notebook))
    (.setName (:name notebook))
    (.setUpdateSequenceNum (:update-seq-num notebook))
    (.setDefaultNotebook (:default? notebook))
    (.setServiceCreated (:created-at notebook))
    (.setServiceUpdated (:updated-at notebook))))
    
(defn get-user-store [user-store-url]
  (let [http-client (THttpClient. user-store-url)
        user-store-prot (TBinaryProtocol. http-client)
        user-store (UserStore$Client. user-store-prot user-store-prot)]
    user-store))

(defn get-note-store [user-store-url dev-token]
  (let [user-store (get-user-store user-store-url)
        note-store-url (.getNoteStoreUrl ^UserStore$Client user-store dev-token)
        note-store-trans (THttpClient. note-store-url)
        _ (.setCustomHeader note-store-trans "User-Agent" "everbox (https://github.com/xumingming/everbox)")
        note-store-prot (TBinaryProtocol. note-store-trans)
        note-store (NoteStore$Client. note-store-prot note-store-prot)]
    {:store note-store
     :dev-token dev-token}))
