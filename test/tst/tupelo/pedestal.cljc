;   Copyright (c) Alan Thompson. All rights reserved. 
;   The use and distribution terms for this software are covered by the Eclipse Public
;   License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the
;   file epl-v10.html at the root of this distribution.  By using this software in any
;   fashion, you are agreeing to be bound by the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns tst.tupelo.pedestal
  (:use tupelo.pedestal tupelo.test)
  (:require
    [schema.core :as s]
  ))

#?(:clj
   (do

     (dotest
       (is= (table-route '{:path         "/todo/:list-id/:item"
                           :verb         :delete
                           :interceptors echo})
         '["/todo/:list-id/:item" :delete echo])

       (is= (table-route '{:path         "/todo/:list-id/:item"
                           :verb         :delete
                           :interceptors echo
                           :route-name   :list-item-delete})
         '["/todo/:list-id/:item" :delete echo :route-name :list-item-delete])

       (is= (table-route '{:path         "/todo/:list-id/:item"
                           :verb         :delete
                           :interceptors echo
                           :constraints  url-rules})
         '["/todo/:list-id/:item" :delete echo :constraints url-rules])

       (is= (table-route '{:path         "/todo/:list-id/:item"
                           :verb         :delete
                           :interceptors [echo]
                           :route-name   :list-item-delete
                           :constraints  url-rules})
         '["/todo/:list-id/:item" :delete [echo] :route-name :list-item-delete :constraints url-rules])

       )

     ))
