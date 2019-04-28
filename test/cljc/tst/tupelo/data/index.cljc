;   Copyright (c) Alan Thompson. All rights reserved.
;   The use and distribution terms for this software are covered by the Eclipse Public License 1.0
;   (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file epl-v10.html at
;   the root of this distribution.  By using this software in any fashion, you are agreeing to be
;   bound by the terms of this license.  You must not remove this notice, or any other, from this
;   software.
(ns tst.tupelo.data.index
  (:use tupelo.core)
  #?(:clj (:refer-clojure :exclude [load ->VecNode]))
  #?(:clj (:require
            [tupelo.test :refer [define-fixture deftest dotest dotest-focus is isnt is= isnt= is-set= is-nonblank= testing throws?]]
            [tupelo.core :as t :refer [spy spyx spyxx]]
            [tupelo.data :as td]
            [tupelo.data.index :as tdi]
            [tupelo.lexical :as lex]
            [clojure.data.avl :as avl]
            [schema.core :as s]
            [clojure.walk :as walk]))
  #?(:cljs (:require
             [tupelo.test-cljs :refer [define-fixture deftest dotest is isnt is= isnt= is-set= is-nonblank= testing throws?]
              :include-macros true]
             [tupelo.core :as t :refer [spy spyx spyxx] :include-macros true]
             [tupelo.data :as td]
             [tupelo.lexical :as lex]
             [clojure.data.avl :as avl]
             [schema.core :as s]
             ))
  )

; #todo fix for cljs
; #todo fix dotest-focus so it works again!

#?(:cljs (enable-console-print!))

(dotest
  (is= (vec (avl/sorted-set-by lex/compare-lex [1 :a] [1] [2]))
    [[1] [1 :a] [2]])
  (is= (vec (avl/sorted-set-by lex/compare-lex [1 :a] [1 nil] [1] [2]))
    [[1] [1 nil] [1 :a] [2]])
  (let [expected-vec [[1]
                      [1 nil]
                      [1 :a]
                      [1 :b]
                      [1 :b nil]
                      [1 :b nil 9]
                      [1 :b 3]
                      [2]
                      [2 0]
                      [3]
                      [3 :y]]
        expected-set (tdi/->sorted-set-avl expected-vec)
        data         (reverse expected-vec)
        result-set   (apply avl/sorted-set-by lex/compare-lex data)
        result-vec   (vec result-set)]
    (is= result-vec expected-vec)
    (is= result-set expected-set) )
  (let [expected   [[1]
                    [1 nil]
                    [1 nil nil]
                    [1 nil 9]
                    [1 2]
                    [1 2 nil]
                    [1 2 3]]
        data       (reverse expected)
        result-vec (vec (tdi/->sorted-set-avl data))]
    (is= result-vec expected)))

(dotest
  (let [lex-set (avl/sorted-set 1 2 3)
        lex-map (avl/sorted-map :a 1 :b 2 :c 3)]
    (s/validate lex/SortedSetType lex-set)
    (s/validate lex/SortedMapType lex-map)
    (is= #{1 2 3} lex-set)
    (is= {:a 1 :b 2 :c 3} lex-map))

  (let [data-raw (tdi/->sorted-set-avl #{[:b 1] [:b 2] [:b 3]
                                         [:f 1] [:f 2] [:f 3]
                                         [:h 1] [:h 2]})]
    ; test with prefix-key
    (is= (tdi/split-key-prefix (tdi/bound-lower [:a 2]) data-raw)
      {:smaller #{},
       :matches #{},
       :larger  #{[:b 1] [:b 2] [:b 3] [:f 1] [:f 2] [:f 3] [:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix (tdi/bound-lower [:b 2]) data-raw)
      {:smaller #{}
       :matches #{[:b 1] [:b 2] [:b 3]},
       :larger  #{[:f 1] [:f 2] [:f 3] [:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix (tdi/bound-lower [:c 2]) data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3]},
       :matches #{}
       :larger  #{[:f 1] [:f 2] [:f 3] [:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix (tdi/bound-lower [:f 2]) data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3]},
       :matches #{[:f 1] [:f 2] [:f 3]},
       :larger  #{[:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix (tdi/bound-lower [:g 2]) data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3] [:f 1] [:f 2] [:f 3]},
       :matches #{},
       :larger  #{[:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix (tdi/bound-lower [:h 2]) data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3] [:f 1] [:f 2] [:f 3]},
       :matches #{[:h 1] [:h 2]}
       :larger  #{}})
    (is= (tdi/split-key-prefix (tdi/bound-lower [:joker 2]) data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3] [:f 1] [:f 2] [:f 3] [:h 1] [:h 2]},
       :matches #{}
       :larger  #{}}))

  ; test with full-key
  (let [data-raw (tdi/->sorted-set-avl #{[:b 1] [:b 2] [:b 3]
                                         [:f 1] [:f 2] [:f 3]
                                         [:h 1] [:h 2]})]
    (is= (tdi/split-key-prefix [:a 2] data-raw)
      {:smaller #{},
       :matches #{},
       :larger  #{[:b 1] [:b 2] [:b 3] [:f 1] [:f 2] [:f 3] [:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix [:b 2] data-raw)
      {:smaller #{[:b 1]}
       :matches #{ [:b 2] },
       :larger  #{[:b 3] [:f 1] [:f 2] [:f 3] [:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix [:c 2] data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3]},
       :matches #{}
       :larger  #{[:f 1] [:f 2] [:f 3] [:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix [:f 2] data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3] [:f 1]},
       :matches #{[:f 2]},
       :larger  #{[:f 3] [:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix [:g 2] data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3] [:f 1] [:f 2] [:f 3]},
       :matches #{},
       :larger  #{[:h 1] [:h 2]}})
    (is= (tdi/split-key-prefix [:h 2] data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3] [:f 1] [:f 2] [:f 3] [:h 1]},
       :matches #{ [:h 2]}
       :larger  #{}})
    (is= (tdi/split-key-prefix [:joker 2] data-raw)
      {:smaller #{[:b 1] [:b 2] [:b 3] [:f 1] [:f 2] [:f 3] [:h 1] [:h 2]},
       :matches #{}
       :larger  #{}}))

  )



