package edu.rpi.legup.controller;

import edu.rpi.legup.app.GameBoardFacade;
import edu.rpi.legup.app.LegupPreferences;
import edu.rpi.legup.history.*;
import edu.rpi.legup.model.gameboard.CaseBoard;
import edu.rpi.legup.model.rules.CaseRule;
import edu.rpi.legup.model.rules.ContradictionRule;
import edu.rpi.legup.model.rules.DirectRule;
import edu.rpi.legup.model.rules.Rule;
import edu.rpi.legup.model.tree.TreeElementType;
import edu.rpi.legup.model.tree.TreeNode;
import edu.rpi.legup.ui.boardview.BoardView;
import edu.rpi.legup.ui.boardview.ElementView;
import edu.rpi.legup.ui.proofeditorui.treeview.TreeElementView;
import edu.rpi.legup.ui.proofeditorui.treeview.TreeView;
import edu.rpi.legup.ui.proofeditorui.treeview.TreeViewSelection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * {@code ProofController} is a {@code BoardController} to be used in the proof editor to interact
 * the game board and use rules. Provides functionality for executing rules and defining behavior
 * when the user interacts with the board.
 */
public abstract class ProofController <ViewType extends BoardView> extends BoardController<ViewType> {

    /** Constructs a controller to be used in the proof editor. */
    public ProofController() {}

    /**
     * Executes an {@link EditDataCommand} for this interaction with the specified {@code ElementView}.
     * Behavior for this action is delegated to {@link #doElementSelection(ElementView, int)} and
     * {@link #undoElementSelection(ElementView, int)}.
     *
     * @param view {@code ElementView} being selected
     * @param mouseButton mouse button used to select view
     */
    @Override
    public void selectElement(@NotNull ElementView view, int mouseButton) {
        TreeViewSelection selection = GameBoardFacade.getInstance().getLegupUI()
                .getTreePanel().getTreeView().getSelection();
        attemptCommand(new EditDataCommand(view, selection, mouseButton,
                this::doElementSelection, this::undoElementSelection));
    }

    /**
     * Performs a selection or "clicking" action on the specified element view. This method is distinct
     * from {@link #selectElement} as that method will fire the {@link EditDataCommand} that will
     * ultimately call this method to enact the changes.
     *
     * @param view {@code ElementView} being selected
     * @param mouseButton mouse button used to select view
     */
    public abstract void doElementSelection(@NotNull ElementView view, int mouseButton);

    /**
     * Undoes a selection or "clicking" action on the specified element view. This method undoes the
     * behavior defined in {@link #doElementSelection(ElementView, int)} so that {@link EditDataCommand}
     * can undo when it is removed from the history.
     *
     * @param view {@code ElementView} that was selected
     * @param mouseButton mouse button used to select view
     */
    public abstract void undoElementSelection(@NotNull ElementView view, int mouseButton);

    /**
     * Event handler for when the user selects a rule.
     *
     * @param rule {@code Rule} that has been selected
     */
    public void ruleSelected(@NotNull Rule rule) {
        switch (rule) {
            case DirectRule directRule -> selectDirectRule(directRule);
            case ContradictionRule contraRule -> selectContraRule(contraRule);
            case CaseRule caseRule -> selectCaseRule(caseRule);
            default -> {}
        }
    }

    /**
     * Helper method for {@link #ruleSelected(Rule)} that is triggered when a {@code DirectRule}
     * is selected.
     *
     * @param rule {@code DirectRule} that has been selected
     */
    protected void selectDirectRule(DirectRule rule) {
        TreeViewSelection selection = GameBoardFacade.getInstance().getLegupUI()
                .getTreePanel().getTreeView().getSelection();

        attemptCommand(LegupPreferences.allowDefaultRules()
                ? new ApplyDefaultDirectRuleCommand(selection, rule)
                : new ValidateDirectRuleCommand(selection, rule));
    }

    /**
     * Helper method for {@link #ruleSelected(Rule)} that is triggered when a {@code ContradictionRule}
     * is selected.
     *
     * @param rule {@code ContradictionRule} that has been selected
     */
    protected void selectContraRule(ContradictionRule rule) {
        TreeViewSelection selection = GameBoardFacade.getInstance().getLegupUI()
                .getTreePanel().getTreeView().getSelection();

        attemptCommand(new ValidateContradictionRuleCommand(selection, rule));
    }

    /**
     * Helper method for {@link #ruleSelected(Rule)} that is triggered when a {@code CaseRule}
     * is selected.
     *
     * @param rule {@code CaseRule} that has been selected
     */
    protected void selectCaseRule(CaseRule rule) {
        TreeView treeView = GameBoardFacade.getInstance().getLegupUI().getTreePanel().getTreeView();
        TreeViewSelection selection = treeView.getSelection();
        List<TreeElementView> selectedViews = selection.getSelectedViews();

        // Attempt auto-generating cases if rule is being applied to a single tree node
        if (selectedViews.size() == 1 &&
                selectedViews.getFirst().getTreeElement().getType() == TreeElementType.NODE) {

            if (LegupPreferences.autoGenerateCases()) {
                String error = "";
                TreeNode treeNode = (TreeNode) selectedViews.getFirst().getTreeElement();

                if (!treeNode.getChildren().isEmpty()) {
                    error = "Cases cannot be generated from a node with children.";
                } else {
                    // Try/Catch in case the rule is canceled by user (e.g. Skyscraper cellForNumber)
                    try {
                        CaseBoard caseBoard = rule.getApplicableLocationsBoard(treeNode.getBoard());
                        if (caseBoard != null && caseBoard.getCount() > 0) {
                            GameBoardFacade.getInstance().getPuzzleModule()
                                    .notifyBoardListeners(
                                            listener -> listener.onCaseBoardAdded(caseBoard));
                        } else {
                            error = "This board cannot be applied with this case rule.";
                        }
                    }
                    catch (Exception e) {
                        error = e.getMessage();
                    }
                }
                treeView.updateError(error);
            }
            else {
                treeView.updateError("Auto-generated case rules are disabled.");
            }
        }
        else {
            attemptCommand(new ValidateCaseRuleCommand(selection, rule));
        }
    }

    /**
     * Helper method that attempts to execute a {@code PuzzleCommand} and displays its error to
     * the {@link TreeView} if it fails.
     *
     * @param command {@code PuzzleCommand} to attempt execution of
     */
    protected void attemptCommand(PuzzleCommand command) {
        String error = "";
        if (command.canExecute()) {
            command.execute();
            GameBoardFacade.getInstance().getHistory().pushChange(command);
        } else {
            error = command.getError();
        }
        GameBoardFacade.getInstance().getLegupUI().getTreePanel().getTreeView().updateError(error);
    }
}
