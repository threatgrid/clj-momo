(ns clj-momo.lib.net
  (:import java.net.ServerSocket))

(defn available-port []
  (loop [port 3000]
    (if (try (with-open [sock (ServerSocket. port)]
               (.getLocalPort sock))
             (catch Exception e nil))
      port
      (recur (+ port 1)))))
