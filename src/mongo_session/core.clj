(ns mongo-session.core
  (:use [ring.middleware.session.store :only [SessionStore]]
        [somnium.congomongo :only [fetch fetch-one insert! update! destroy!]])
  (:import java.util.UUID))

(defn new-key []
  (str (UUID/randomUUID)))

(defrecord MongoSessionStore [coll]
  SessionStore
  (read-session [this key]
    (-> (:data (fetch-one coll
                          :where {:_id key}
                          :only [:data]))
        (or {})))
  (write-session [this key data]
    (let [key (or key (new-key))]
      (update! coll {:_id key} {:$set {:data data}})
      key))
  (delete-session [this key]
    (destroy! coll {:_id key})
    nil))

(defn mongo-session
  "Create a mongodb store for ring sessions. The store will use whatever
  mongodb connection you have open, and assumes that it has ownership of the
  collection named by coll."
  [coll]
  (MongoSessionStore. coll))

(defn list-session-ids [store]
  (map :_id (fetch (:coll store) :only [:_id])))
