(ns tst.tupelo.array
  #?@(:clj [
  (:use tupelo.array
        tupelo.test)
  (:require
    [schema.test :as st]
    [tupelo.array :as tar]
    [tupelo.misc :as tm]
    [tupelo.impl :as i]
    [tupelo.string :as ts]
    [clojure.string :as str])
            ]) )

(use-fixtures :once st/validate-schemas)

#?(:clj (do
(dotest
  (let [a34  (tar/create 3 4 :a)
        a34f (flatten a34)]
    (is= 3 (count a34) (tar/num-rows a34))
    (is= 4 (count (a34 0)) (tar/num-cols a34))
    (is= 12 (count a34f))
    (is (every? #(= :a %) a34f))
    (is (every? #(= :a %) (i/forv [ii (range (tar/num-rows a34))
                                 jj (range (tar/num-cols a34))]
                            (tar/elem-get a34 ii jj)))))

  (let [a34  (tar/create 3 4)
        a34f (flatten a34)]
    (is= 3 (count a34) (tar/num-rows a34))
    (is= 4 (count (a34 0)) (tar/num-cols a34))
    (is= 12 (count a34f))
    (is (every? nil? a34f))
    (is (every? nil? (i/forv [ii (range (tar/num-rows a34))
                            jj (range (tar/num-cols a34))]
                       (tar/elem-get a34 ii jj)))))

  (let [a34               (atom (tar/create 3 4))
        target            [[00 01 02 03]
                           [10 11 12 13]
                           [20 21 22 23]]
        target-rows-vec   [00 01 02 03 10 11 12 13 20 21 22 23]
        target-cols-vec   [00 10 20 01 11 21 02 12 22 03 13 23]

        target-flip-ud    [[20 21 22 23]
                           [10 11 12 13]
                           [00 01 02 03]]

        target-flip-lr    [[03 02 01 00]
                           [13 12 11 10]
                           [23 22 21 20]]

        target-tx         [[00 10 20]
                           [01 11 21]
                           [02 12 22]
                           [03 13 23]]

        target-rot-left-1 [[03 13 23]
                           [02 12 22]
                           [01 11 21]
                           [00 10 20]]
        target-rot-left-2 [[23 22 21 20]
                           [13 12 11 10]
                           [03 02 01 00]]
        target-rot-left-3 [[20 10 00]
                           [21 11 01]
                           [22 12 02]
                           [23 13 03]]
        ]
    (dotimes [ii 3]
      (dotimes [jj 4]
        (swap! a34 tar/elem-set ii jj (+ (* ii 10) jj))))
    (is (ts/equals-ignore-spacing? (tar/toString @a34)
          " 0    1    2    3
           10   11   12   13
           20   21   22   23"))

    (is= (tar/row-get target 0) [00 01 02 03])
    (is= (tar/row-get target 1) [10 11 12 13])
    (is= (tar/row-get target 2) [20 21 22 23])

    (is= (tar/col-get target 0) [00 10 20])
    (is= (tar/col-get target 1) [01 11 21])
    (is= (tar/col-get target 2) [02 12 22])
    (is= (tar/col-get target 3) [03 13 23])

    (is= (array->row-data target) [00 01 02 03
                                   10 11 12 13
                                   20 21 22 23])
    (is= (-> target (transpose) (array->col-data)) [00 01 02 03
                                                    10 11 12 13
                                                    20 21 22 23])

    (is= target-rows-vec (array->row-data target))
    (is= target-cols-vec (array->col-data target))
    (is= target (row-data->array 3 4 target-rows-vec))
    (is= target (col-data->array 3 4 target-cols-vec))
    (is= target (->> target
                  (array->row-data)
                  (row-data->array 3 4)))
    (is= target (->> target
                  (array->col-data)
                  (col-data->array 3 4)))

    (is= target @a34)
    (is= target-flip-ud (tar/flip-ud target))
    (is= target-flip-lr (tar/flip-lr target))
    (is= target-tx      (tar/transpose target))

    (is= target-rot-left-1 (-> target (tar/rot-left)))
    (is= target-rot-left-2 (-> target (tar/rot-left) (tar/rot-left)))
    (is= target-rot-left-3 (-> target (tar/rot-left) (tar/rot-left) (tar/rot-left)))
    (is= target            (-> target (tar/rot-left) (tar/rot-left) (tar/rot-left) (tar/rot-left)))

    (is= target-rot-left-3 (-> target (tar/rot-right)))
    (is= target-rot-left-2 (-> target (tar/rot-right) (tar/rot-right)))
    (is= target-rot-left-1 (-> target (tar/rot-right) (tar/rot-right) (tar/rot-right)))
    (is= target            (-> target (tar/rot-right) (tar/rot-right) (tar/rot-right) (tar/rot-right)))))

(dotest
  (let [demo  [[00 01 02 03]
               [10 11 12 13]
               [20 21 22 23]]
      ]
    (throws? IllegalArgumentException (rows-get demo 0 0))
    (is= (rows-get demo 0 1) [[00 01 02 03]])
    (is= (rows-get demo 0 2) [[00 01 02 03]
                              [10 11 12 13]])
    (is= (rows-get demo 0 3) [[00 01 02 03]
                              [10 11 12 13]
                              [20 21 22 23]])
    (is= (rows-get demo 1 3) [[10 11 12 13]
                              [20 21 22 23]])
    (is= (rows-get demo 2 3) [[20 21 22 23]])
    (throws? IllegalArgumentException (rows-get demo 3 3))

    (is= demo (rows-get demo))
    (is= (rows-get demo [2 0 1]) [[20 21 22 23]
                                  [00 01 02 03]
                                  [10 11 12 13]])
    (is= demo (rows->array [[00 01 02 03]
                            [10 11 12 13]
                            [20 21 22 23]]))
    (throws? (rows->array [[00 01 02 03]
                           [10 11 12   ]
                           [20 21 22 23]]))))

(dotest
  (let [demo [[00 01 02 03]
              [10 11 12 13]
              [20 21 22 23]]
        ]
    (is (thrown? IllegalArgumentException (cols-get demo 0 0)))
    (is= (cols-get demo 0 1) [[00 10 20]])
    (is= (cols-get demo 0 2) [[00 10 20]
                              [01 11 21]])
    (is= (cols-get demo 0 3) [[00 10 20]
                              [01 11 21]
                              [02 12 22]])
    (is= (cols-get demo 0 4) [[00 10 20]
                              [01 11 21]
                              [02 12 22]
                              [03 13 23]])
    (is= (cols-get demo 1 4) [[01 11 21]
                              [02 12 22]
                              [03 13 23]])
    (is= (cols-get demo 2 4) [[02 12 22]
                              [03 13 23]])
    (is= (cols-get demo 3 4) [[03 13 23]])
    (is (thrown? IllegalArgumentException (cols-get demo 4 4)))

    (is= (cols-get demo) (cols-get demo 0 4))
    (is= (cols-get demo [2 0 3 1]) [[02 12 22]
                                    [00 10 20]
                                    [03 13 23]
                                    [01 11 21]])
    (is= demo (cols->array [[00 10 20]
                            [01 11 21]
                            [02 12 22]
                            [03 13 23]]))
    (throws? (cols->array [[00 10 20]
                           [01 11 21]
                           [02 12]
                           [03 13 23]]))))

(dotest
  (is (tar/symmetric? [[1 2]
                       [2 1]]))
  (isnt (tar/symmetric? [[1 3]
                         [2 1]]))
  (is (tar/symmetric? [[1 2 3]
                       [2 4 5]
                       [3 5 6]]))
  (isnt (tar/symmetric? [[1 9 3]
                         [2 4 5]
                         [3 5 6]]))
  (isnt (tar/symmetric? [[1 2 9]
                         [2 4 5]
                         [3 5 6]]))
  )

(dotest
  (let [demo [[00 01 02 03]
              [10 11 12 13]
              [20 21 22 23]]]
    (is= (tar/row-drop demo 0) [[10 11 12 13]
                                [20 21 22 23]])
    (is= (tar/row-drop demo 0 2)
      (tar/row-drop demo 0 2 0 2 0) [[10 11 12 13]])
    (is= (tar/row-drop demo 1 2) [[00 01 02 03]])

    (is= (tar/col-drop demo 1) [[00 02 03]
                                [10 12 13]
                                [20 22 23]])
    (is= (tar/col-drop demo 1 2 2 1) [[00 03]
                                      [10 13]
                                      [20 23]])
    (is= (tar/col-drop demo 0 2 3) [[01]
                                    [11]
                                    [21]])
    (is (thrown? Exception (tar/row-drop demo :x)))))

(dotest
  (let [a13 [[00 01 02]]
        a23 [[00 01 02]
             [10 11 12]]
        a33 [[00 01 02]
             [10 11 12]
             [20 21 22]]
        a34 [[00 01 02 03]
             [10 11 12 13]
             [20 21 22 23]]]
    (is (thrown? Error (tar/row-add a13 [1 2])))
    (is (thrown? Error (tar/row-add a13 [1 2] [1 2 3])))
    (is (thrown? Error (tar/row-add a13 [1 2 3 4])))
    (is= a23 (tar/row-add a13 [10 11 12]))
    (is= a33 (tar/row-add a13 [10 11 12] [20 21 22]))))

(dotest
  (let [a22 [[00 01]
             [10 11]]
        a23 [[00 01 02]
             [10 11 12]]
        a24 [[00 01 02 03]
             [10 11 12 13]]]
    (is (thrown? Error (tar/col-add a23 [1 2 3])))
    (is (thrown? Error (tar/col-add a23 [1 2] [1 2 3])))
    (is= a23 (tar/col-add a22 [2 12]))
    (is= a24 (tar/col-add a22 [2 12] [3 13]))))

(dotest
  (let [a12 [[00 01]]
        a22 [[00 01]
             [10 11]]
        a32 [[00 01]
             [10 11]
             [20 21]]
        a42 [[00 01]
             [10 11]
             [20 21]
             [30 31]]]
    (is (thrown? Error (tar/glue-vert a22 [[1 2 3]])))
    (is= a22 (tar/glue-vert
               [[00 01]]
               [[10 11]]))
    (is= a32
      (tar/glue-vert ; noop
        [[00 01]
         [10 11]
         [20 21]])
      (tar/glue-vert
        [[00 01]]
        [[10 11]]
        [[20 21]])
      (tar/glue-vert
        [[00 01]]
        [[10 11]
         [20 21]])
      (tar/glue-vert
        [[00 01]
         [10 11]]
        [[20 21]]))

    (is= a42
      (tar/glue-vert
        [[00 01]]
        [[10 11]]
        [[20 21]]
        [[30 31]])
      (tar/glue-vert
        [[00 01]
         [10 11]]
        [[20 21]
         [30 31]])
      (tar/glue-vert
        [[00 01]
         [10 11]
         [20 21]]
        [[30 31]])
      (tar/glue-vert
        [[00 01]]
        [[10 11]
         [20 21]
         [30 31]]))))
(dotest
  (let [a21 [[00]
             [10]]
        a22 [[00 01]
             [10 11]]
        a23 [[00 01 02]
             [10 11 12]]
        a24 [[00 01 02 03]
             [10 11 12 13]]]
    (is (thrown? Error (tar/glue-horiz a22 [[1 2 3]])))
    (is= a22 (tar/glue-horiz a21
               [[01]
                [11]]))
    (is= a23 (tar/glue-horiz a21
               [[01]
                [11]]
               [[02]
                [12]]))
    (is= a23 (tar/glue-horiz a21
               [[01 02]
                [11 12]]))

    (is= a24 (tar/glue-horiz a22
               [[02]
                [12]]
               [[03]
                [13]]))
    (is= a24 (tar/glue-horiz a22
               [[02 03]
                [12 13]]))
    (is= a24 (tar/glue-horiz a21
               [[01]
                [11]]
               [[02 03]
                [12 13]]))))
(dotest
  (let [demo
        [[1 2 3]
         [4 5 6]]]
    (is (thrown? Throwable (tar/row-set demo 2 [[1 2 3]])))
    (is (thrown? Throwable (tar/row-set demo 1 [[1 2 3 4]])))
    (is= (tar/row-set demo 1 [7 8 9]) [[1 2 3]
                                       [7 8 9]])

    (is (thrown? Throwable (tar/col-set demo 3 [[1 2]])))
    (is (thrown? Throwable (tar/col-set demo 1 [[1 2 3 4]])))
    (is= (tar/col-set demo 1 [7 8]) [[1 7 3]
                                     [4 8 6]])

    )
  )
))
