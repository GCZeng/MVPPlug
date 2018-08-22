package actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlDocument;

import java.io.File;

public class AndroidUtils {
    public static VirtualFile getAppPackageBaseDir(Project project) {
        String path = project.getBasePath() + File.separator +
                "app" + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "java" + File.separator +
                getAppPackageName(project).replace(".", File.separator);
        return LocalFileSystem.getInstance().findFileByPath(path);
    }

    public static String getAppPackageName(Project project) {
        PsiFile manifestFile = getManifestFile(project);
        XmlDocument xml = (XmlDocument) manifestFile.getFirstChild();
        return xml.getRootTag().getAttribute("package").getValue();
    }

    public static PsiFile getManifestFile(Project project) {
        String path = project.getBasePath() + File.separator +
                "app" + File.separator +
                "src" + File.separator +
                "main" + File.separator +
                "AndroidManifest.xml";
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
        if (virtualFile == null) return null;
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    public static String split(String name) {
        if (name.length() < 1) {
            return name.toLowerCase();
        }
        StringBuffer sb = new StringBuffer();

        int sIndex = 0;
        boolean flag = true;
        for (int i = 1; i < name.length(); i++) {
            if ((name.charAt(i) >= 'A' && name.charAt(i) <= 'Z')) {
                sb.append("_");
                sb.append(name.substring(sIndex, i).toLowerCase());
                sIndex = i;
                flag = false;
            } else if (i == name.length() - 1) {
                sb.append("_");
                sb.append(name.substring(sIndex, i + 1).toLowerCase());
                sIndex = i;
                flag = false;
            }
        }
//        if (flag) {
//            sb.append("_");
//            sb.append(name.toLowerCase());
//        }

        return sb.toString();
    }

}
