This zip file contains the source files for PTViewer 2.8

This version of the applet is based on version 2.5 by Helmut Dersch,
so I have included only the files that are new or have been modified.

The included source code is enough to build the small version of the applet.
To create the large version you will need other classes.
You can find those classes in the V2.5 distribution.

Please note that the source code of V2.5 is not available: only the class files 
are available so you will have to play with the jar tool.

Fulvio Senore
fsenore@ica-net.it

------------------------------
Ver. 2.8改良点
・切替時にローディング画面が出ないよう該当部分をコメントアウト
・起動時に全て渡していたデータを切替時にその都度渡すために
　newPano()をもう一つ追加 (既存のnewPano()とは引数が異なる)
・以前あった切替時の演出機能は組み込めていない