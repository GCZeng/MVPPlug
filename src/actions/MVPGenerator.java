package actions;

import actions.mvphandler.MVPHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MVPGenerator extends AnAction implements MVPHandler.OnGenerateListener {
    private AnActionEvent actionEvent;
    private MVPHandler mvpHandler;
    private Map<String, String> map = new HashMap<>();

    private final String CONTRACT_NAME = "contract";
    private final String CONTRACT_FILE_NAME = "Contract";
    private final String CONTRACT_TEMPLATES_NAME = "GenerateContractFile";

    private final String PRESENTER_NAME = "presenter";
    private final String PRESENTER_FILE_NAME = "Presenter";
    private final String PRESENTER_TEMPLATES_NAME = "GeneratePresenterFile";

    private final String ACTIVITY_NAME = "activity";
    private final String ACTIVITY_FILE_NAME = "Activity";
    private final String ACTIVITY_TEMPLATES_NAME = "GenerateActivityFile";

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        this.actionEvent = anActionEvent;
        mvpHandler = new MVPHandler();
        mvpHandler.setTitle("Create MVP");
        mvpHandler.setOnGenerateListener(this);
        mvpHandler.pack();
        //设置对话框跟随当前windows窗口
        mvpHandler.setLocationRelativeTo(WindowManager.getInstance().getFrame(anActionEvent.getProject()));
        mvpHandler.setVisible(true);
    }


    @Override
    public void onGenerate(String text) {

        JavaDirectoryService directoryService = JavaDirectoryService.getInstance();

        //当前工程
        Project project = actionEvent.getProject();
        map.put("NAME", text);
        map.put("BASE_PACKAGE_NAME", AndroidUtils.getAppPackageName(project));
        map.put("LAYOUT_NAME", AndroidUtils.split(text));

        // app包名根目录 ...\app\src\main\java\PACKAGE_NAME\
        VirtualFile baseDir = AndroidUtils.getAppPackageBaseDir(project);

        //创建Contract
        createFile(text, directoryService, project, baseDir, CONTRACT_NAME, CONTRACT_FILE_NAME, CONTRACT_TEMPLATES_NAME);

        //创建Presenter
        createFile(text, directoryService, project, baseDir, PRESENTER_NAME, PRESENTER_FILE_NAME, PRESENTER_TEMPLATES_NAME);

        //创建Activity
        VirtualFile contractDir = createDir(baseDir, "ui");
        createFile(text, directoryService, project, contractDir, ACTIVITY_NAME, ACTIVITY_FILE_NAME, ACTIVITY_TEMPLATES_NAME);

        //创建布局文件
        VirtualFile baseFile = project.getBaseDir();
        VirtualFile[] childFiles = baseFile.getChildren();
        VirtualFile virtualFile = getOutputPath(childFiles);
        virtualFile = virtualFile.findChild("layout");

        String layoutName = "activity" + map.get("LAYOUT_NAME") + ".xml";

        if (virtualFile.findChild(layoutName) == null) {
            try {
                virtualFile = virtualFile.createChildData(null, layoutName);
                String xmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                        "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n" +
                        "    style=\"@style/ll_m_m_v\">\n\n" +
                        "    <" + map.get("BASE_PACKAGE_NAME") + ".TitleView\n" +
                        "        android:id=\"@+id/titleView\"\n" +
                        "        android:style=\"@style/BaseTitleStyle\"\n" +
                        "        />\n</LinearLayout>";
                virtualFile.setBinaryContent(xmlStr.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private VirtualFile getOutputPath(VirtualFile[] virtualFiles) {
        for (VirtualFile virtualFile : virtualFiles) {
            String name = virtualFile.getName();
            VirtualFile[] childVirtualFile = virtualFile.getChildren();

            if (name.equals("res")) {
                return virtualFile;
            } else if (childVirtualFile.length > 0) {
                VirtualFile resPath = getOutputPath(childVirtualFile);
                if (resPath != null) {
                    return resPath;
                }
            }
        }
        return null;
    }


    /**
     * 创建类
     *
     * @param text
     * @param directoryService
     * @param project
     * @param baseDir
     * @param contractName
     * @param contractFileName
     * @param contractTemplatesName
     */
    private void createFile(String text, JavaDirectoryService directoryService, Project project, VirtualFile baseDir, String contractName, String contractFileName, String contractTemplatesName) {
        // 判断根目录下是否有contract_name文件夹
        VirtualFile contractDir = createDir(baseDir, contractName);
        PsiDirectory contractPsiDirectory = PsiManager.getInstance(project).findDirectory(contractDir);
        if (contractPsiDirectory.getName().contains(contractName) && contractPsiDirectory.findFile(text + contractFileName + ".java") == null) {
            directoryService.createClass(contractPsiDirectory, text + contractFileName, contractTemplatesName, true, map);
        }
    }

    @Nullable
    private VirtualFile createDir(VirtualFile baseDir, String contractName) {
        VirtualFile contractDir = baseDir.findChild(contractName);
        if (contractDir == null) {
            try {
                contractDir = baseDir.createChildDirectory(null, contractName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contractDir;
    }

    @Override
    public void onCancel() {
        mvpHandler.setVisible(false);
    }
}
