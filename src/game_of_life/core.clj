(ns game-of-life.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

; Define the height of the world
(def dim 20)

(defstruct cell :alive)

(def world
  (apply vector
       (map (fn [_]
              (apply vector
                     (map (fn [_] (ref (struct cell false)))
                                      (range dim))))
                   (range dim))))

(defn place [[x y]]
  (-> world (nth x) (nth y)))

(defn make-alive
  "Make a cell become alive. Counts from the top left corner"
  [[x y]]
  (sync nil
        (alter (place [x y]) assoc :alive true)))

(defn setup
  "Places initial starting pattern of alive cells"
  []
  (sync nil
        (let [alive-cell-coords-list [[1 1] [1 2] [1 3]
                                      [2 1] [2 2] [2 3]
                                      [3 1] [3 2] [3 3]]]
          (do (for [alive-cell-coords alive-cell-coords-list]
                (make-alive alive-cell-coords))
              world))))

(import
 '(java.awt Color Graphics Dimension)
 '(java.awt.image BufferedImage)
 '(javax.swing JPanel JFrame))


(def scale 20)

(defn fill-cell [#^Graphics g x y c]
  (doto g
    (.setColor c)
    (.fillRect (* x scale) (* y scale) scale scale)))

(defn render-cell [g p x y]
  (when (true? (:alive p))
    (fill-cell g x y (new Color 0 255 0 255))))

(defn render [g]
  (let [v (dosync (apply vector (for [x (range dim) y (range dim)]
                                  @(place [x y]))))
        img (new BufferedImage (* scale dim) (* scale dim)
                 (. BufferedImage TYPE_INT_ARGB))
        bg (. img (getGraphics))]
    (doto bg
      (.setColor (. Color white))
      (.fillRect 0 0 (. img (getWidth)) (. img (getHeight))))
    (dorun
     (for [x (range dim) y (range dim)]
       (render-cell bg (v (+ (* x dim) y)) x y)))
    (. g (drawImage img 0 0 nil))
    (. bg (dispose))))

(def panel (doto (proxy [JPanel] []
                   (paint [g] (render g)))
             (.setPreferredSize (new Dimension
                                     (* scale dim)
                                     (* scale dim)))))

(def frame (doto (new JFrame)
             (.add panel)
             .pack
             .show))

(. panel (repaint))

(def myworld (setup))



