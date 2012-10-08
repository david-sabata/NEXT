NEXT
====

Neat EXtensible Tasklist


Jak nastavit Git & GitHub
-------------------------
Možná jste na to narazili už při úvodním průvodci - pro práci s Gitem je potřeba mít na GitHubu asociované SSH klíče s vaším účtem. [Návod je tady](https://help.github.com/articles/generating-ssh-keys), klíče se pak vkládají [tu](https://github.com/settings/ssh).

Potom vlezete v terminálu do složky kde chcete mít projektové soubory (v repozitáři jsou přímo soubory, nadřazená složka tam není). Tam naklepete postupně
`git init`
`git remote add origin git@github.com:david-sabata/NEXT.git`
`git pull origin master`. Tím si stáhnete aktuální repo k sobě.

Pak už je klasický postup přes `git status`, `git add`, `git commit` a `git push`. Pro stažení pak `git pull`. Kdyžtak si na to najděte někde guide jestli jste s Gitem ještě nedělali :-)